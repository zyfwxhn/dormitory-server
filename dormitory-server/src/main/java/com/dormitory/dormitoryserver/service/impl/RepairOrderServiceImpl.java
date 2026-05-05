package com.dormitory.dormitoryserver.service.impl;

import com.dormitory.dormitoryserver.constant.StatusConstant;
import com.dormitory.dormitoryserver.context.BaseContext;
import com.dormitory.dormitoryserver.dto.*;
import com.dormitory.dormitoryserver.entity.Notification;
import com.dormitory.dormitoryserver.entity.RepairOrder;
import com.dormitory.dormitoryserver.entity.Student;
import com.dormitory.dormitoryserver.exception.BaseException;
import com.dormitory.dormitoryserver.mapper.NotificationMapper;
import com.dormitory.dormitoryserver.mapper.RepairOrderMapper;
import com.dormitory.dormitoryserver.mapper.StudentMapper;
import com.dormitory.dormitoryserver.mapper.WorkerMapper;
import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.service.RepairOrderService;
import com.dormitory.dormitoryserver.websocket.WebSocketServer;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class RepairOrderServiceImpl implements RepairOrderService {

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    // 注入 StudentMapper，用来查询当前学生的宿舍地址生成快照
    @Autowired
    private StudentMapper studentMapper;

    // 【1. 新增注入】WebSocket 和 通知持久化 Mapper
    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private WorkerMapper workerMapper;

    @Override
    public void submitOrder(RepairOrderSubmitDTO dto) {
        RepairOrder repairOrder = new RepairOrder();
        BeanUtils.copyProperties(dto, repairOrder);

        // 1. 获取当前登录学生 ID
        Long studentId = BaseContext.getCurrentId();
        repairOrder.setStudentId(studentId);

        // 2. 核心逻辑：生成地址快照 (Address Snapshot)
        Student student = studentMapper.getById(studentId);
        String snapshot = student.getBuildingNo() + "-" + student.getRoomNo();
        repairOrder.setAddressSnapshot(snapshot);

        // 3. 补充基础属性
        repairOrder.setStatus(0); // 0: 待处理
        repairOrder.setCreateTime(LocalDateTime.now());
        repairOrder.setUpdateTime(LocalDateTime.now());

        // 4. 落库
        repairOrderMapper.insert(repairOrder);
    }

    /**
     * 历史报修订单分页查询
     * @param queryDTO 分页和条件参数
     * @return 统一分页结果
     */
    @Override
    public PageResult pageQuery(RepairOrderPageQueryDTO queryDTO) {
        log.info("学生分页查询历史报修单：{}", queryDTO);

        // 1. 启动 PageHelper 分页（拦截器会在执行下一条 SQL 时自动追加 LIMIT 语句）
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());

        // 2. 从线程上下文中获取当前登录的学生 ID（防止横向越权的核心）
        Long currentStudentId = BaseContext.getCurrentId();

        // 3. 调用 Mapper 层查库（返回值必须用 Page 接收）
        Page<RepairOrder> page = repairOrderMapper.pageQuery(queryDTO, currentStudentId);

        // 4. 封装成咱们系统的统一下发格式 PageResult
        // page.getTotal() 取出总条数，page.getResult() 取出当前页的数据集合
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 查询报修单详情
     * @param id 订单ID
     * @return 报修单详细信息
     */
    @Override
    public RepairOrder getDetail(Long id) {
        log.info("查询报修单详情，订单ID: {}", id);
        // 1. 获取当前登录学生 ID
        Long currentStudentId = BaseContext.getCurrentId();

        // 2. 携带订单ID和学生ID去查库，如果查不到（要么是不存在，要么是别人的单子），返回null
        RepairOrder repairOrder = repairOrderMapper.getByIdAndStudentId(id, currentStudentId);

        return repairOrder;
    }

    /**
     * 管理端：历史报修订单动态分页查询
     * @param queryDTO 管理端分页和高级筛选条件
     * @return 统一分页结果
     */
    @Override
    public PageResult adminPageQuery(RepairOrderAdminPageQueryDTO queryDTO) {
        log.info("管理端分页查询全校报修单：{}", queryDTO);

        // 1. 启动分页拦截
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());

        // 2. 直接调用管理端专用的 Mapper 方法（注意：这里绝不传入 BaseContext 的当前ID）
        Page<RepairOrder> page = repairOrderMapper.adminPageQuery(queryDTO);

        // 3. 封装标准分页结果
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 维修员接单/更新维修状态 (状态流转)
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 【2. 新增事务保证】确保更新订单和发通知同时成功或失败
    public void updateStatus(RepairOrderUpdateStatusDTO dto) {
        log.info("维修员端报修单状态流转：{}", dto);

        // 获取当前登录的维修员ID
        Long currentWorkerId = BaseContext.getCurrentId();

        // 查出数据库里这笔单子的现状
        RepairOrder order = repairOrderMapper.getById(dto.getId());
        if (order == null) {
            throw new BaseException("报修单不存在");
        }

        Integer currentStatus = order.getStatus();
        Integer targetStatus = dto.getStatus();

        RepairOrder repairOrder = new RepairOrder();
        repairOrder.setId(dto.getId());
        repairOrder.setStatus(targetStatus);

        // 场景 A：接单动作
        if (targetStatus == 1) {
            if (currentStatus != 0) {
                throw new BaseException("手慢了，该订单已被接单或取消！");
            }
            repairOrder.setWorkerId(currentWorkerId);
        }
        // 场景 B：更新进度动作
        else if (targetStatus == 2 || targetStatus == 3) {
            if (!currentWorkerId.equals(order.getWorkerId())) {
                throw new BaseException("非法越权操作：您无权修改他人的报修单状态！");
            }
            if (targetStatus == 2 && currentStatus != 1) {
                throw new BaseException("状态流转异常：只能对处于【已接单】的订单执行【开始维修】操作！");
            }
            if (targetStatus == 3 && currentStatus != 2) {
                throw new BaseException("状态流转异常：只能对处于【维修中】的订单执行【完成】操作！");
            }

            // 完工拍照强校验
            if (targetStatus == 3) {
                String finishImages = dto.getFinishImages();
                if (finishImages == null || finishImages.trim().isEmpty()) {
                    throw new BaseException("为了保障服务质量，维修完工必须上传现场照片！");
                }
                repairOrder.setFinishImages(finishImages);
            }
        } else {
            throw new BaseException("非法的目标状态！");
        }

        // 调用 Mapper 层执行动态更新
        repairOrder.setUpdateTime(LocalDateTime.now());
        repairOrderMapper.update(repairOrder);

        // 【3. 新增核心逻辑】触发 WebSocket 实时推送
        if (targetStatus == 1 || targetStatus == 3) {
            Notification notification = new Notification();
            // 拿到下单学生的ID，作为推送的唯一目标
            notification.setStudentId(order.getStudentId());
            notification.setType(1); // 业务类型：1-系统报修通知
            notification.setIsRead(0); // 0-未读
            notification.setCreateTime(LocalDateTime.now());

            String messageContent = "";
            if (targetStatus == 1) {
                notification.setTitle("报修单已接单");
                messageContent = "您的报修单师傅已接单，正在为您安排维修！";
            } else if (targetStatus == 3) {
                notification.setTitle("报修单已完成");
                messageContent = "您的报修单已维修完成，请前往查看并评价！";
            }
            notification.setContent(messageContent);

            // 第一道防线：存入数据库（离线持久化）
            notificationMapper.insert(notification);

            // 第二道防线：WebSocket 在线实时推送
            String wsMsg = String.format("{\"type\":\"repair_status_changed\",\"status\":%d}", targetStatus);
            webSocketServer.sendToSpecificClient(order.getStudentId().toString(), wsMsg);
            log.info("已向学生 {} 实时推送报修进度更新", order.getStudentId());
        }
    }

    @Override
    public void evaluate(RepairOrderEvaluationDTO dto) {
        // 1. 获取当前登录学生的ID (从ThreadLocal上下文获取)
        Long studentId = BaseContext.getCurrentId();

        // 2. 根据订单ID查询报修单 (复用现有的 getById 方法)
        RepairOrder order = repairOrderMapper.getById(dto.getId());
        if (order == null) {
            throw new BaseException("报修单不存在");
        }

        // 3. 【核心】防横向越权校验：判断该订单是不是当前学生的
        if (!order.getStudentId().equals(studentId)) {
            // 抛出异常，绝不姑息越权操作
            throw new BaseException("非法操作：无权评价他人的报修单");
        }

        // 4. 【核心】状态机校验：只有"已完成(status=3)"的订单才能评价
        if (order.getStatus() != 3) {
            throw new BaseException("当前订单状态不允许评价");
        }

        // 5. 防重复评价：检查是否已经评价过
        if (order.getEvaluationScore() != null) {
            throw new BaseException("该订单已经评价过，无需重复评价");
        }

        // 6. 组装实体，准备更新
        RepairOrder updateEntity = new RepairOrder();
        updateEntity.setId(dto.getId());
        updateEntity.setEvaluationScore(dto.getEvaluationScore());
        updateEntity.setEvaluationContent(dto.getEvaluationContent());
        updateEntity.setUpdateTime(LocalDateTime.now());

        // 7. 调用 Mapper 层执行动态更新
        repairOrderMapper.update(updateEntity);
    }

    @Override
    public void cancelOrder(Long id) {
        Long currentStudentId = BaseContext.getCurrentId();

        RepairOrder order = repairOrderMapper.getById(id);
        if (order == null) {
            throw new BaseException("报修单不存在");
        }

        // 防横向越权：只能取消自己的单子
        if (!order.getStudentId().equals(currentStudentId)) {
            throw new BaseException("非法操作：无权取消他人的报修单");
        }

        // 状态机约束：只有待处理状态(0)允许取消
        if (order.getStatus() != 0) {
            throw new BaseException("当前状态不可取消，该订单可能已被接单或处理");
        }

        // 【关键修复】：依据你的数据库设计，状态 4 为已取消！
        RepairOrder updateEntity = new RepairOrder();
        updateEntity.setId(id);
        updateEntity.setStatus(4); // 修改这里，绝对不能是 -1

        repairOrderMapper.update(updateEntity);
    }

    @Override
    public RepairOrder getDetailById(Long id) {
        RepairOrder order = repairOrderMapper.getById(id);
        if (order == null) {
            throw new BaseException("报修单不存在");
        }
        return order;
    }

    @Override
    public void autoDispatch(Long orderId) {
        log.info("开始为报修单 {} 进行智能派单", orderId);

        // 1. 查出这笔报修单的详情
        RepairOrder order = repairOrderMapper.getById(orderId);
        if (order == null) {
            throw new BaseException("报修单不存在");
        }
        // 防并发：只能派单给待处理(status=0)的订单
        if (order.getStatus() != 0) {
            throw new BaseException("该订单已被处理，无法重复派单！");
        }
        String repairType = order.getRepairType();

        // 2. 【核心】尝试调用智能匹配算法：专业对口 + 最空闲
        Long workerId = workerMapper.getSmartDispatchWorkerId(repairType);

        // 3. 【企业级高可用降级】如果对口师傅全都不在岗，或者没有配置对应技能的师傅
        if (workerId == null) {
            log.warn("未找到完全对口的空闲师傅，触发降级派单策略，寻找全局最空闲师傅...");
            // 退化使用之前的普通空闲查询
            workerId = workerMapper.getIdleWorkerId();
            if (workerId == null) {
                throw new BaseException("当前无可用或启用的维修员，派单失败！请联系管理员。");
            }
        }

        // 4. 构造要更新的订单对象
        RepairOrder updateEntity = new RepairOrder();
        updateEntity.setId(orderId);
        updateEntity.setWorkerId(workerId);
        // 自动派单后，状态直接流转为 1 (已接单)
        updateEntity.setStatus(1);
        updateEntity.setUpdateTime(java.time.LocalDateTime.now());

        // 5. 执行更新入库
        repairOrderMapper.update(updateEntity);

        log.info("派单成功，报修单 {} (故障类型:{}) 已智能分配给维修员 {}", orderId, repairType, workerId);
    }

    @Override
    public PageResult workerPageQuery(RepairOrderPageQueryDTO dto) {
        // 1. 开启 PageHelper 分页拦截器
        PageHelper.startPage(dto.getPage(), dto.getPageSize());

        // 2. 获取当前登录的维修员ID（数据隔离的核心）
        Long currentWorkerId = BaseContext.getCurrentId();

        // 3. 调用 Mapper 层专属的维修员查询方法
        Page<RepairOrder> page = repairOrderMapper.workerPageQuery(dto, currentWorkerId);

        // 4. 封装统一分页结果
        return new PageResult(page.getTotal(), page.getResult());
    }
}
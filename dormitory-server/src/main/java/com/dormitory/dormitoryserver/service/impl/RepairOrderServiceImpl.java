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

    @Autowired
    private StudentMapper studentMapper;

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

        Long studentId = BaseContext.getCurrentId();
        repairOrder.setStudentId(studentId);

        Student student = studentMapper.getById(studentId);
        String snapshot = student.getBuildingNo() + "-" + student.getRoomNo();
        repairOrder.setAddressSnapshot(snapshot);

        repairOrder.setStatus(0);
        repairOrder.setCreateTime(LocalDateTime.now());
        repairOrder.setUpdateTime(LocalDateTime.now());

        repairOrderMapper.insert(repairOrder);
    }

    @Override
    public PageResult pageQuery(RepairOrderPageQueryDTO queryDTO) {
        log.info("学生分页查询报修单: {}", queryDTO);
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Long currentStudentId = BaseContext.getCurrentId();
        Page<RepairOrder> page = repairOrderMapper.pageQuery(queryDTO, currentStudentId);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public RepairOrder getDetail(Long id) {
        log.info("查询报修单详情, 订单ID: {}", id);
        Long currentStudentId = BaseContext.getCurrentId();
        return repairOrderMapper.getByIdAndStudentId(id, currentStudentId);
    }

    @Override
    public PageResult adminPageQuery(RepairOrderAdminPageQueryDTO queryDTO) {
        log.info("管理端分页查询报修单: {}", queryDTO);
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<RepairOrder> page = repairOrderMapper.adminPageQuery(queryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(RepairOrderUpdateStatusDTO dto) {
        log.info("维修员更新工单状态: {}", dto);

        Long currentWorkerId = BaseContext.getCurrentId();
        RepairOrder order = repairOrderMapper.getById(dto.getId());
        if (order == null) {
            throw new BaseException("报修单不存在");
        }

        Integer currentStatus = order.getStatus();
        Integer targetStatus = dto.getStatus();

        RepairOrder repairOrder = new RepairOrder();
        repairOrder.setId(dto.getId());
        repairOrder.setStatus(targetStatus);

        if (targetStatus == 1) { // 接单
            if (currentStatus != 0) {
                throw new BaseException("该订单已被接单或取消");
            }
            repairOrder.setWorkerId(currentWorkerId);
        } else if (targetStatus == 2 || targetStatus == 3) { // 开始维修 / 完成
            if (!currentWorkerId.equals(order.getWorkerId())) {
                throw new BaseException("无权修改他人的工单");
            }
            if (targetStatus == 2 && currentStatus != 1) {
                throw new BaseException("只能对已接单的订单开始维修");
            }
            if (targetStatus == 3 && currentStatus != 2) {
                throw new BaseException("只能对维修中的订单标记完成");
            }
            if (targetStatus == 3) {
                String finishImages = dto.getFinishImages();
                if (finishImages == null || finishImages.trim().isEmpty()) {
                    throw new BaseException("维修完工须上传现场照片");
                }
                repairOrder.setFinishImages(finishImages);
            }
        } else {
            throw new BaseException("非法的目标状态");
        }

        repairOrder.setUpdateTime(LocalDateTime.now());
        repairOrderMapper.update(repairOrder);

        // 接单或完工时通知学生
        if (targetStatus == 1 || targetStatus == 3) {
            Notification notification = new Notification();
            notification.setStudentId(order.getStudentId());
            notification.setType(1);
            notification.setIsRead(0);
            notification.setCreateTime(LocalDateTime.now());

            if (targetStatus == 1) {
                notification.setTitle("报修单已接单");
                notification.setContent("您的报修单已被接单, 维修员正在安排维修.");
            } else {
                notification.setTitle("报修单已完成");
                notification.setContent("您的报修单已维修完成, 请评价.");
            }
            notificationMapper.insert(notification);

            String wsMsg = String.format("{\"type\":\"repair_status_changed\",\"status\":%d}", targetStatus);
            webSocketServer.sendToSpecificClient(order.getStudentId().toString(), wsMsg);
            log.info("已推送报修状态更新给学生 {}", order.getStudentId());
        }
    }

    @Override
    public void evaluate(RepairOrderEvaluationDTO dto) {
        Long studentId = BaseContext.getCurrentId();
        RepairOrder order = repairOrderMapper.getById(dto.getId());
        if (order == null) {
            throw new BaseException("报修单不存在");
        }
        if (!order.getStudentId().equals(studentId)) {
            throw new BaseException("无权评价他人的报修单");
        }
        if (order.getStatus() != 3) {
            throw new BaseException("只能评价已完成的报修单");
        }
        if (order.getEvaluationScore() != null) {
            throw new BaseException("该报修单已评价过");
        }
        if (dto.getEvaluationScore() < 1 || dto.getEvaluationScore() > 5) {
            throw new BaseException("评分须在1到5之间");
        }

        RepairOrder updateEntity = new RepairOrder();
        updateEntity.setId(dto.getId());
        updateEntity.setEvaluationScore(dto.getEvaluationScore());
        updateEntity.setEvaluationContent(dto.getEvaluationContent());
        updateEntity.setUpdateTime(LocalDateTime.now());
        repairOrderMapper.update(updateEntity);
    }

    @Override
    public void cancelOrder(Long id) {
        Long studentId = BaseContext.getCurrentId();
        RepairOrder order = repairOrderMapper.getById(id);
        if (order == null) {
            throw new BaseException("报修单不存在");
        }
        if (!order.getStudentId().equals(studentId)) {
            throw new BaseException("无权取消他人的报修单");
        }
        if (order.getStatus() != 0) {
            throw new BaseException("只能取消待处理的报修单");
        }

        RepairOrder update = new RepairOrder();
        update.setId(id);
        update.setStatus(4);
        update.setUpdateTime(LocalDateTime.now());
        repairOrderMapper.update(update);
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
        log.info("智能派单, 订单ID={}", orderId);
        RepairOrder order = repairOrderMapper.getById(orderId);
        if (order == null) {
            throw new BaseException("报修单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BaseException("该订单已被处理, 无法重复派单");
        }

        String repairType = order.getRepairType();
        Long workerId = workerMapper.getSmartDispatchWorkerId(repairType);
        if (workerId == null) {
            workerId = workerMapper.getIdleWorkerId();
        }
        if (workerId == null) {
            throw new BaseException("当前没有可用的维修员");
        }

        RepairOrder update = new RepairOrder();
        update.setId(orderId);
        update.setWorkerId(workerId);
        update.setStatus(1);
        update.setUpdateTime(LocalDateTime.now());
        repairOrderMapper.update(update);
        log.info("已将报修单 {} 派单给维修员 {}", orderId, workerId);
    }

    @Override
    public PageResult workerPageQuery(RepairOrderPageQueryDTO dto) {
        Long workerId = BaseContext.getCurrentId();
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<RepairOrder> page = repairOrderMapper.workerPageQuery(dto, workerId);
        return new PageResult(page.getTotal(), page.getResult());
    }
}

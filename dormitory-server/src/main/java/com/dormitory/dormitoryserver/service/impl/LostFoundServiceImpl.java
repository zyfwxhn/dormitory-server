package com.dormitory.dormitoryserver.service.impl;

import com.dormitory.dormitoryserver.context.BaseContext;
import com.dormitory.dormitoryserver.dto.LostFoundPageQueryDTO;
import com.dormitory.dormitoryserver.dto.LostFoundSubmitDTO;
import com.dormitory.dormitoryserver.dto.LostFoundUpdateStatusDTO;
import com.dormitory.dormitoryserver.dto.ViolationReviewDTO;
import com.dormitory.dormitoryserver.entity.LostFound;
import com.dormitory.dormitoryserver.exception.BaseException;
import com.dormitory.dormitoryserver.mapper.LostFoundMapper;
import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.service.LostFoundService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LostFoundServiceImpl implements LostFoundService {

    @Autowired
    private LostFoundMapper lostFoundMapper;

    @Override
    public void publish(LostFoundSubmitDTO dto) {
        // 1. 创建实体对象
        LostFound lostFound = new LostFound();

        // 2. 属性拷贝：将 DTO 中同名的属性（标题、描述等）一键拷贝到实体类中
        BeanUtils.copyProperties(dto, lostFound);

        // 3. 组装后端掌控的私有/敏感字段
        // 安全获取当前登录学生ID
        Long studentId = BaseContext.getCurrentId();
        lostFound.setStudentId(studentId);

        // 初始化状态：0 (寻找中/待认领)
        lostFound.setStatus(0);

        // 备注：createTime 和 updateTime 已经在数据库建表时设置了默认生成，所以这里无需手动 set

        // 4. 调用 Mapper 插入数据库
        lostFoundMapper.insert(lostFound);
    }

    @Override
    public PageResult pageQuery(LostFoundPageQueryDTO dto) {
        // 1. 开启 PageHelper 分页拦截器 (传入页码和每页条数)
        PageHelper.startPage(dto.getPage(), dto.getPageSize());

        // 公共大厅：默认只查”寻找中/待认领(0)”，防止看到已撤销的信息
        // 个人主页（传了studentId）：显示所有状态
        if (dto.getStudentId() == null && dto.getStatus() == null) {
            dto.setStatus(0);
        }

        // 3. 调用 Mapper 层进行查询 (PageHelper 会自动将返回的 List 强转为 Page 对象)
        Page<LostFound> page = lostFoundMapper.pageQuery(dto);

        // 4. 封装成咱们统一定义的 PageResult 返回给前端
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void updateStatus(LostFoundUpdateStatusDTO dto) {
        // 1. 查询原数据，验证是否存在
        LostFound lostFound = lostFoundMapper.getById(dto.getId());
        if (lostFound == null) {
            throw new BaseException("该信息不存在");
        }

        // 2. 【核心防御】防横向越权校验：必须是本人的发布信息才能修改
        Long currentStudentId = BaseContext.getCurrentId();
        if (!lostFound.getStudentId().equals(currentStudentId)) {
            throw new BaseException("非法操作：无权修改他人的发布信息");
        }

        // 3. 【状态机防御】只有处于“寻找中(0)”的记录，才能修改状态
        if (lostFound.getStatus() != 0) {
            throw new BaseException("该信息已完结，无法再次修改状态");
        }

        // 4. 【合法性防御】只能修改为 已解决(1) 或 已撤销(2)
        if (dto.getStatus() != 1 && dto.getStatus() != 2) {
            throw new BaseException("非法的目标状态");
        }

        // 5. 组装更新实体并调用 Mapper
        LostFound updateEntity = new LostFound();
        updateEntity.setId(dto.getId());
        updateEntity.setStatus(dto.getStatus());

        lostFoundMapper.update(updateEntity);
    }

    @Override
    public LostFound getById(Long id) {
        // 同样直接调用之前写好的 Mapper 方法
        return lostFoundMapper.getById(id);
    }

    @Override
    public void violate(ViolationReviewDTO dto) {
        log.info("管理员触发违规下架失物招领，ID：{}, 原因：{}", dto.getId(), dto.getReason());

        LostFound lostFound = new LostFound();
        lostFound.setId(dto.getId());
        // 状态 2 表示：已撤销/已下架
        lostFound.setStatus(2);
        lostFound.setUpdateTime(java.time.LocalDateTime.now());

        // 复用已有的 update 方法
        lostFoundMapper.update(lostFound);
    }
}
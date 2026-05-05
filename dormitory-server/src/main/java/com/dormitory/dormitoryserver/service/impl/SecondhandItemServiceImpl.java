package com.dormitory.dormitoryserver.service.impl;

import com.dormitory.dormitoryserver.context.BaseContext;
import com.dormitory.dormitoryserver.dto.SecondhandItemPageQueryDTO;
import com.dormitory.dormitoryserver.dto.SecondhandItemSubmitDTO;
import com.dormitory.dormitoryserver.dto.SecondhandItemUpdateStatusDTO;
import com.dormitory.dormitoryserver.dto.ViolationReviewDTO;
import com.dormitory.dormitoryserver.entity.SecondhandItem;
import com.dormitory.dormitoryserver.exception.BaseException;
import com.dormitory.dormitoryserver.mapper.SecondhandItemMapper;
import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.service.SecondhandItemService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class SecondhandItemServiceImpl implements SecondhandItemService {

    @Autowired
    private SecondhandItemMapper secondhandItemMapper;

    @Override
    public void publish(SecondhandItemSubmitDTO dto) {
        // 1. 创建实体对象
        SecondhandItem secondhandItem = new SecondhandItem();

        // 2. 属性拷贝：将 DTO 中同名的属性（名称、价格、描述等）一键拷贝到实体类中
        BeanUtils.copyProperties(dto, secondhandItem);

        // 3. 组装后端掌控的私有/敏感字段
        // 安全获取当前登录学生ID，作为卖家ID
        Long studentId = BaseContext.getCurrentId();
        secondhandItem.setStudentId(studentId);

        // 初始化状态：0 (在售)
        secondhandItem.setStatus(0);
        secondhandItem.setCreateTime(LocalDateTime.now());
        secondhandItem.setUpdateTime(LocalDateTime.now());

        // 4. 调用 Mapper 插入数据库
        secondhandItemMapper.insert(secondhandItem);
    }

    @Override
    public PageResult pageQuery(SecondhandItemPageQueryDTO dto) {
        // 1. 开启 PageHelper 分页拦截器
        PageHelper.startPage(dto.getPage(), dto.getPageSize());

        // 公共大厅：默认只查”在售(0)”状态的商品
        // 个人主页（传了studentId）：显示所有状态
        if (dto.getStudentId() == null && dto.getStatus() == null) {
            dto.setStatus(0);
        }

        // 3. 调用 Mapper 层进行查询
        Page<SecondhandItem> page = secondhandItemMapper.pageQuery(dto);

        // 4. 封装成 PageResult 返回
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void updateStatus(SecondhandItemUpdateStatusDTO dto) {
        // 1. 查询原数据，验证是否存在
        SecondhandItem secondhandItem = secondhandItemMapper.getById(dto.getId());
        if (secondhandItem == null) {
            throw new BaseException("该商品不存在");
        }

        // 2. 【核心防御】防横向越权校验：必须是卖家本人才能操作
        Long currentStudentId = BaseContext.getCurrentId();
        if (!secondhandItem.getStudentId().equals(currentStudentId)) {
            throw new BaseException("非法操作：无权修改他人的商品状态");
        }

        // 3. 【状态机防御】只有处于“在售(0)”的商品，才能修改状态
        if (secondhandItem.getStatus() != 0) {
            throw new BaseException("该商品已售出或已下架，无法再次修改状态");
        }

        // 4. 【合法性防御】只能修改为 已售出(1) 或 已下架(2)
        if (dto.getStatus() != 1 && dto.getStatus() != 2) {
            throw new BaseException("非法的目标状态");
        }

        // 5. 组装更新实体并调用 Mapper
        SecondhandItem updateEntity = new SecondhandItem();
        updateEntity.setId(dto.getId());
        updateEntity.setStatus(dto.getStatus());

        secondhandItemMapper.update(updateEntity);
    }

    @Override
    public SecondhandItem getById(Long id) {
        // 直接调用我们上一步已经写好的 Mapper 方法即可！
        return secondhandItemMapper.getById(id);
    }

    @Override
    public PageResult adminPageQuery(SecondhandItemPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<SecondhandItem> page = secondhandItemMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void violate(ViolationReviewDTO dto) {
        log.info("管理员触发违规下架二手商品，ID：{}, 原因：{}", dto.getId(), dto.getReason());

        // 1. 检查存在性
        SecondhandItem exist = secondhandItemMapper.getById(dto.getId());
        if (exist == null) {
            throw new BaseException("该商品不存在");
        }
        // 2. 只有待审核状态才能下架
        if (exist.getStatus() != 0) {
            throw new BaseException("该商品已被处理，无法重复下架");
        }

        SecondhandItem item = new SecondhandItem();
        item.setId(dto.getId());
        item.setStatus(2);
        item.setUpdateTime(LocalDateTime.now());
        secondhandItemMapper.update(item);
    }
}
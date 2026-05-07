package com.dormitory.dormitoryserver.service.impl;

import com.dormitory.dormitoryserver.context.BaseContext;
import com.dormitory.dormitoryserver.dto.SecondhandItemPageQueryDTO;
import com.dormitory.dormitoryserver.dto.SecondhandItemSubmitDTO;
import com.dormitory.dormitoryserver.dto.SecondhandItemUpdateStatusDTO;
import com.dormitory.dormitoryserver.dto.ViolationReviewDTO;
import com.dormitory.dormitoryserver.entity.Notification;
import com.dormitory.dormitoryserver.entity.SecondhandItem;
import com.dormitory.dormitoryserver.exception.BaseException;
import com.dormitory.dormitoryserver.mapper.NotificationMapper;
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

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public void publish(SecondhandItemSubmitDTO dto) {
        SecondhandItem secondhandItem = new SecondhandItem();
        BeanUtils.copyProperties(dto, secondhandItem);

        Long studentId = BaseContext.getCurrentId();
        secondhandItem.setStudentId(studentId);
        secondhandItem.setStatus(0);
        secondhandItem.setCreateTime(LocalDateTime.now());
        secondhandItem.setUpdateTime(LocalDateTime.now());

        secondhandItemMapper.insert(secondhandItem);
    }

    @Override
    public PageResult pageQuery(SecondhandItemPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        // 大厅默认只展示在售商品, 个人主页查全部
        if (dto.getStudentId() == null && dto.getStatus() == null) {
            dto.setStatus(0);
        }
        Page<SecondhandItem> page = secondhandItemMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void updateStatus(SecondhandItemUpdateStatusDTO dto) {
        SecondhandItem secondhandItem = secondhandItemMapper.getById(dto.getId());
        if (secondhandItem == null) {
            throw new BaseException("该商品不存在");
        }
        Long currentStudentId = BaseContext.getCurrentId();
        if (!secondhandItem.getStudentId().equals(currentStudentId)) {
            throw new BaseException("无权修改他人的商品状态");
        }
        if (secondhandItem.getStatus() != 0) {
            throw new BaseException("该商品已售出或已下架");
        }
        if (dto.getStatus() != 1 && dto.getStatus() != 2) {
            throw new BaseException("非法的目标状态");
        }

        SecondhandItem updateEntity = new SecondhandItem();
        updateEntity.setId(dto.getId());
        updateEntity.setStatus(dto.getStatus());
        secondhandItemMapper.update(updateEntity);
    }

    @Override
    public SecondhandItem getById(Long id) {
        return secondhandItemMapper.getById(id);
    }

    @Override
    public void edit(SecondhandItemSubmitDTO dto, Long id) {
        SecondhandItem exist = secondhandItemMapper.getById(id);
        if (exist == null) throw new BaseException("该商品不存在");
        Long currentId = BaseContext.getCurrentId();
        if (!exist.getStudentId().equals(currentId)) throw new BaseException("无权编辑他人的商品");
        if (exist.getStatus() != 0) throw new BaseException("已售出或已下架的商品无法编辑");

        SecondhandItem update = new SecondhandItem();
        BeanUtils.copyProperties(dto, update);
        update.setId(id);
        update.setUpdateTime(LocalDateTime.now());
        secondhandItemMapper.update(update);
        log.info("学生 {} 编辑二手商品 ID={}", currentId, id);
    }

    @Override
    public PageResult adminPageQuery(SecondhandItemPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<SecondhandItem> page = secondhandItemMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void violate(ViolationReviewDTO dto) {
        log.info("管理员下架二手商品, ID={}", dto.getId());

        SecondhandItem exist = secondhandItemMapper.getById(dto.getId());
        if (exist == null) {
            throw new BaseException("该商品不存在");
        }
        if (exist.getStatus() != 0) {
            throw new BaseException("该商品已被处理");
        }

        SecondhandItem item = new SecondhandItem();
        item.setId(dto.getId());
        item.setStatus(2);
        item.setUpdateTime(LocalDateTime.now());
        secondhandItemMapper.update(item);

        Notification noti = new Notification();
        noti.setStudentId(exist.getStudentId());
        noti.setTitle("你的商品已被下架");
        noti.setContent("你发布的「" + exist.getName() + "」因违规被管理员下架.");
        noti.setType(4);
        noti.setIsRead(0);
        noti.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(noti);
        log.info("已通知学生 {} 其二手商品被下架", exist.getStudentId());
    }
}

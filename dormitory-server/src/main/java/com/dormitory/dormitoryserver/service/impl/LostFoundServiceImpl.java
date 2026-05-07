package com.dormitory.dormitoryserver.service.impl;

import com.dormitory.dormitoryserver.context.BaseContext;
import com.dormitory.dormitoryserver.dto.LostFoundPageQueryDTO;
import com.dormitory.dormitoryserver.dto.LostFoundSubmitDTO;
import com.dormitory.dormitoryserver.dto.LostFoundUpdateStatusDTO;
import com.dormitory.dormitoryserver.dto.ViolationReviewDTO;
import com.dormitory.dormitoryserver.entity.LostFound;
import com.dormitory.dormitoryserver.entity.Notification;
import com.dormitory.dormitoryserver.entity.Student;
import com.dormitory.dormitoryserver.exception.BaseException;
import com.dormitory.dormitoryserver.mapper.LostFoundMapper;
import com.dormitory.dormitoryserver.mapper.NotificationMapper;
import com.dormitory.dormitoryserver.mapper.StudentMapper;
import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.service.LostFoundService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class LostFoundServiceImpl implements LostFoundService {

    @Autowired
    private LostFoundMapper lostFoundMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Override
    public void publish(LostFoundSubmitDTO dto) {
        
        LostFound lostFound = new LostFound();
        BeanUtils.copyProperties(dto, lostFound);
        
        Long studentId = BaseContext.getCurrentId();
        lostFound.setStudentId(studentId);

        // 初始化状态: 0 (寻找中/待认领)
        lostFound.setStatus(0);
        lostFound.setCreateTime(LocalDateTime.now());
        lostFound.setUpdateTime(LocalDateTime.now());
        lostFoundMapper.insert(lostFound);
    }

    @Override
    public PageResult pageQuery(LostFoundPageQueryDTO dto) {
        
        PageHelper.startPage(dto.getPage(), dto.getPageSize());

        // 大厅默认只展示活跃信息
        if (dto.getStudentId() == null && dto.getStatus() == null) {
            dto.setStatus(0);
        }
        Page<LostFound> page = lostFoundMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void updateStatus(LostFoundUpdateStatusDTO dto) {
        LostFound lostFound = lostFoundMapper.getById(dto.getId());
        if (lostFound == null) {
            throw new BaseException("该信息不存在");
        }
        Long currentStudentId = BaseContext.getCurrentId();
        if (!lostFound.getStudentId().equals(currentStudentId)) {
            throw new BaseException("无权修改他人的发布信息");
        }
        if (lostFound.getStatus() != 0) {
            throw new BaseException("该信息已完结,无法再次修改状态");
        }
        if (dto.getStatus() != 1 && dto.getStatus() != 2) {
            throw new BaseException("非法的目标状态");
        }

        LostFound updateEntity = new LostFound();
        updateEntity.setId(dto.getId());
        updateEntity.setStatus(dto.getStatus());

        lostFoundMapper.update(updateEntity);
    }

    @Override
    public LostFound getById(Long id) {
        
        return lostFoundMapper.getById(id);
    }

    @Override
    public void violate(ViolationReviewDTO dto) {
        log.info("管理员触发违规下架失物招领,ID: {}, 原因: {}", dto.getId(), dto.getReason());
        LostFound exist = lostFoundMapper.getById(dto.getId());
        if (exist == null) {
            throw new BaseException("该信息不存在");
        }
        if (exist.getStatus() != 0) {
            throw new BaseException("该信息已被处理,无法重复下架");
        }

        LostFound lostFound = new LostFound();
        lostFound.setId(dto.getId());
        lostFound.setStatus(2);
        lostFound.setUpdateTime(java.time.LocalDateTime.now());
        lostFoundMapper.update(lostFound);
        Notification noti = new Notification();
        noti.setStudentId(exist.getStudentId());
        noti.setTitle("你的发布信息已被下架");
        noti.setContent("你发布的「" + exist.getTitle() + "」因违规被管理员下架.");
        noti.setType(4);
        noti.setIsRead(0);
        noti.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(noti);
        log.info("已通知学生 {} 其失物招领信息被下架", exist.getStudentId());
    }

    @Override
    public PageResult adminPageQuery(LostFoundPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<LostFound> page = lostFoundMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void edit(LostFoundSubmitDTO dto, Long id) {
        LostFound lf = lostFoundMapper.getById(id);
        if (lf == null) throw new BaseException("该信息不存在");
        Long currentId = BaseContext.getCurrentId();
        if (!lf.getStudentId().equals(currentId)) throw new BaseException("无权编辑他人的发布信息");
        if (lf.getStatus() != 0) throw new BaseException("已完结的信息无法编辑");

        LostFound update = new LostFound();
        BeanUtils.copyProperties(dto, update);
        update.setId(id);
        update.setUpdateTime(LocalDateTime.now());
        lostFoundMapper.update(update);
        log.info("学生 {} 编辑失物招领 ID={}", currentId, id);
    }

    @Override
    public void claim(Long lostFoundId) {
        
        LostFound lf = lostFoundMapper.getById(lostFoundId);
        if (lf == null) {
            throw new BaseException("该信息不存在");
        }
        if (lf.getStatus() != 0) {
            throw new BaseException("该信息已完结");
        }
        Long claimerId = BaseContext.getCurrentId();
        if (claimerId.equals(lf.getStudentId())) {
            throw new BaseException("不能认领自己发布的信息");
        }

        Student claimer = studentMapper.getById(claimerId);
        Notification noti = new Notification();
        noti.setStudentId(lf.getStudentId());
        noti.setType(4);
        noti.setIsRead(0);
        noti.setCreateTime(LocalDateTime.now());

        if (lf.getType() == 1) {
            // 失物招领: 有人捡到东西,失主来认领
            noti.setTitle("有人想认领你的信息");
            noti.setContent(claimer.getName() + " (手机 " + claimer.getPhone() + ")想认领你发布的「" + lf.getTitle() + "」,请主动联系对方.");
        } else {
            // 寻物启事: 有人丢了东西,捡到者来提供线索
            noti.setTitle("有人可能捡到了你的物品");
            noti.setContent(claimer.getName() + " (手机 " + claimer.getPhone() + ")可能捡到了你丢失的「" + lf.getTitle() + "」,请主动联系对方.");
        }
        notificationMapper.insert(noti);

        log.info("学生 {} 发起联系请求,类型={}, 失物招领ID={}", claimerId, lf.getType(), lostFoundId);
    }
}
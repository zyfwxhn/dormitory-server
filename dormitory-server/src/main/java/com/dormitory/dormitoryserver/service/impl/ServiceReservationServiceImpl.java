package com.dormitory.dormitoryserver.service.impl;

import com.dormitory.dormitoryserver.context.BaseContext;
import com.dormitory.dormitoryserver.dto.ReservationSubmitDTO;
import com.dormitory.dormitoryserver.entity.ServiceReservation;
import com.dormitory.dormitoryserver.exception.BaseException;
import com.dormitory.dormitoryserver.mapper.ServiceReservationMapper;
import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.service.ServiceReservationService;
import com.dormitory.dormitoryserver.vo.AvailableTimeSlotVO;
import com.dormitory.dormitoryserver.websocket.WebSocketServer;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ServiceReservationServiceImpl implements ServiceReservationService {

    @Autowired
    private ServiceReservationMapper serviceReservationMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private WebSocketServer webSocketServer;

    @Override
    @Transactional
    public void submitReservation(ReservationSubmitDTO dto) {
        log.info("接收到预约请求: {}", dto);

        // 1. 业务基础校验
        LocalDateTime now = LocalDateTime.now();
        LocalDate reserveDate = dto.getReservationDate();
        LocalTime reserveStartTime = dto.getStartTime();

        if (reserveDate.isBefore(now.toLocalDate())) {
            throw new BaseException("不能预约过去的日期！");
        }
        if (reserveDate.isEqual(now.toLocalDate()) && reserveStartTime.isBefore(now.toLocalTime())) {
            throw new BaseException("不能预约今天已经过去的时间段！");
        }
        if (dto.getStartTime().isAfter(dto.getEndTime()) || dto.getStartTime().equals(dto.getEndTime())) {
            throw new BaseException("结束时间必须晚于开始时间！");
        }

        Long currentStudentId = BaseContext.getCurrentId();

        // 2. 尝试 Redis 分布式锁 (Redis 不可用时降级为纯数据库校验)
        String lockKey = String.format("lock:reservation:device:%d:date:%s:time:%s",
                dto.getDeviceId(),
                dto.getReservationDate().toString(),
                dto.getStartTime().toString());
        boolean redisLocked = false;

        try {
            Boolean got = stringRedisTemplate.opsForValue()
                    .setIfAbsent(lockKey, String.valueOf(currentStudentId), 2, TimeUnit.HOURS);
            if (Boolean.FALSE.equals(got)) {
                throw new BaseException("手慢了！该设备的这个时段已被其他同学抢占, 请重新选择时间.");
            }
            redisLocked = true;
        } catch (Exception e) {
            log.warn("Redis 不可用, 降级为数据库校验: {}", e.getMessage());
        }

        try {
            // 数据库冲突检测
            int conflictCount = serviceReservationMapper.checkConflict(
                    dto.getDeviceId(),
                    dto.getReservationDate(),
                    dto.getStartTime(),
                    dto.getEndTime()
            );

            if (conflictCount > 0) {
                throw new BaseException("该时间段已被预约, 请重新选择时间.");
            }

            // 4. 数据入库
            ServiceReservation reservation = new ServiceReservation();
            BeanUtils.copyProperties(dto, reservation);

            reservation.setStudentId(currentStudentId);
            reservation.setStatus(0); // 待使用
            reservation.setCreateTime(LocalDateTime.now());
            reservation.setUpdateTime(LocalDateTime.now());

            serviceReservationMapper.insert(reservation);
            log.info("预约成功, 预约单ID: {}", reservation.getId());

        } finally {
            if (redisLocked) {
                try { stringRedisTemplate.delete(lockKey); } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public List<AvailableTimeSlotVO> getAvailableSlots(Long deviceId, LocalDate reservationDate) {
        List<ServiceReservation> occupiedList = serviceReservationMapper
                .getValidReservationsByDeviceAndDate(deviceId, reservationDate);

        LocalTime businessStart = LocalTime.of(8, 0);
        LocalTime businessEnd = LocalTime.of(22, 0);

        List<AvailableTimeSlotVO> slots = new ArrayList<>();
        LocalTime slotStart = businessStart;

        // 按固定1小时划分时段, 过滤已被预约的
        while (slotStart.isBefore(businessEnd)) {
            LocalTime slotEnd = slotStart.plusHours(1);
            if (slotEnd.isAfter(businessEnd)) {
                slotEnd = businessEnd;
            }
            // 跳过已过去的时间段 (仅限今天)
            if (reservationDate.equals(LocalDate.now()) && slotStart.isBefore(LocalTime.now())) {
                slotStart = slotEnd;
                continue;
            }
            // 检查是否与已占用时段冲突
            boolean conflicted = false;
            for (ServiceReservation r : occupiedList) {
                if (slotStart.isBefore(r.getEndTime()) && slotEnd.isAfter(r.getStartTime())) {
                    conflicted = true;
                    break;
                }
            }
            if (!conflicted) {
                slots.add(new AvailableTimeSlotVO(slotStart, slotEnd));
            }
            slotStart = slotEnd;
        }
        return slots;
    }

    @Override
    public void cancelReservation(Long id) {
        log.info("学生请求取消预约, 预约单ID: {}", id);

        ServiceReservation reservation = serviceReservationMapper.getById(id);
        if (reservation == null) {
            throw new BaseException("预约记录不存在！");
        }

        Long currentStudentId = BaseContext.getCurrentId();
        if (!reservation.getStudentId().equals(currentStudentId)) {
            throw new BaseException("非法操作: 无权取消他人的预约！");
        }

        if (reservation.getStatus() != 0) {
            throw new BaseException("当前状态无法取消！");
        }

        LocalDateTime reserveStartTime = LocalDateTime.of(reservation.getReservationDate(), reservation.getStartTime());
        if (LocalDateTime.now().isAfter(reserveStartTime)) {
            throw new BaseException("预约时间已开始或已过期, 无法取消！");
        }

        // 【核心修复】: 依据毕设表结构设计, 状态 2 为已取消 (绝不能写成 3)
        serviceReservationMapper.updateStatus(id, 2, LocalDateTime.now());
        // 广播时段释放消息, 其他学生可刷新可用时段
        webSocketServer.sendToAllClient("{\"type\":\"reservation_changed\"}");
    }

    @Override
    public PageResult pageQuery(Integer page, Integer pageSize) {
        Long studentId = BaseContext.getCurrentId();
        PageHelper.startPage(page, pageSize);
        Page<ServiceReservation> p = serviceReservationMapper.pageByStudentId(studentId);
        return new PageResult(p.getTotal(), p.getResult());
    }

    @Override
    public PageResult adminPageQuery(Integer page, Integer pageSize, Integer status, String studentNo) {
        PageHelper.startPage(page, pageSize);
        Page<ServiceReservation> p = serviceReservationMapper.adminPageQuery(status, studentNo);
        return new PageResult(p.getTotal(), p.getResult());
    }
}
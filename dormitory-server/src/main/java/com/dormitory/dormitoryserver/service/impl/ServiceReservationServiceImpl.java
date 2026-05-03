package com.dormitory.dormitoryserver.service.impl;

import com.dormitory.dormitoryserver.context.BaseContext;
import com.dormitory.dormitoryserver.dto.ReservationSubmitDTO;
import com.dormitory.dormitoryserver.entity.ServiceReservation;
import com.dormitory.dormitoryserver.exception.BaseException;
import com.dormitory.dormitoryserver.mapper.ServiceReservationMapper;
import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.service.ServiceReservationService;
import com.dormitory.dormitoryserver.vo.AvailableTimeSlotVO;
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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ServiceReservationServiceImpl implements ServiceReservationService {

    @Autowired
    private ServiceReservationMapper serviceReservationMapper;

    // 【新增】注入 SpringBoot 的 Redis 模板
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional // 开启事务
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

        // 2. 构造唯一锁标识
        String lockKey = String.format("lock:reservation:device:%d:date:%s:time:%s",
                dto.getDeviceId(),
                dto.getReservationDate().toString(),
                dto.getStartTime().toString());

        Long currentStudentId = BaseContext.getCurrentId();

        // 尝试获取锁
        Boolean isLocked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, String.valueOf(currentStudentId), 2, TimeUnit.HOURS);

        if (Boolean.FALSE.equals(isLocked)) {
            throw new BaseException("手慢了！该设备的这个时段已被其他同学抢占，请重新选择时间。");
        }

        try {
            // 3. 拿到锁后，数据库兜底防重
            int conflictCount = serviceReservationMapper.checkConflict(
                    dto.getDeviceId(),
                    dto.getReservationDate(),
                    dto.getStartTime(),
                    dto.getEndTime()
            );

            if (conflictCount > 0) {
                throw new BaseException("数据异常！该时间段已被预约，请重新选择时间。");
            }

            // 4. 数据入库
            ServiceReservation reservation = new ServiceReservation();
            BeanUtils.copyProperties(dto, reservation);

            reservation.setStudentId(currentStudentId);
            reservation.setStatus(0); // 待使用
            reservation.setCreateTime(LocalDateTime.now());
            reservation.setUpdateTime(LocalDateTime.now());

            serviceReservationMapper.insert(reservation);
            log.info("预约成功，预约单ID: {}", reservation.getId());

        } finally {
            // 【核心修复】：使用 finally 兜底，无论上面是成功落库，还是校验抛异常
            // 只要你拿到了锁，就必须把它删掉，让出资源！
            stringRedisTemplate.delete(lockKey);
        }
    }

    @Override
    public List<AvailableTimeSlotVO> getAvailableSlots(Long deviceId, LocalDate reservationDate) {
        // [原封不动保留你原有的优秀错峰推荐算法]
        List<ServiceReservation> occupiedList = serviceReservationMapper.getValidReservationsByDeviceAndDate(deviceId, reservationDate);
        occupiedList.sort(Comparator.comparing(ServiceReservation::getStartTime));

        LocalTime businessStart = LocalTime.of(8, 0);
        LocalTime businessEnd = LocalTime.of(22, 0);

        List<AvailableTimeSlotVO> availableSlots = new ArrayList<>();
        LocalTime currentPointer = businessStart;

        for (ServiceReservation occupied : occupiedList) {
            if (currentPointer.isBefore(occupied.getStartTime())) {
                availableSlots.add(new AvailableTimeSlotVO(currentPointer, occupied.getStartTime()));
            }
            if (currentPointer.isBefore(occupied.getEndTime())) {
                currentPointer = occupied.getEndTime();
            }
        }

        if (currentPointer.isBefore(businessEnd)) {
            availableSlots.add(new AvailableTimeSlotVO(currentPointer, businessEnd));
        }

        return availableSlots;
    }

    @Override
    public void cancelReservation(Long id) {
        log.info("学生请求取消预约，预约单ID：{}", id);

        ServiceReservation reservation = serviceReservationMapper.getById(id);
        if (reservation == null) {
            throw new BaseException("预约记录不存在！");
        }

        Long currentStudentId = BaseContext.getCurrentId();
        if (!reservation.getStudentId().equals(currentStudentId)) {
            throw new BaseException("非法操作：无权取消他人的预约！");
        }

        if (reservation.getStatus() != 0) {
            throw new BaseException("当前状态无法取消！");
        }

        LocalDateTime reserveStartTime = LocalDateTime.of(reservation.getReservationDate(), reservation.getStartTime());
        if (LocalDateTime.now().isAfter(reserveStartTime)) {
            throw new BaseException("预约时间已开始或已过期，无法取消！");
        }

        // 【核心修复】：依据毕设表结构设计，状态 2 为已取消（绝不能写成 3）
        serviceReservationMapper.updateStatus(id, 2, LocalDateTime.now());
    }

    @Override
    public PageResult pageQuery(Integer page, Integer pageSize) {
        Long studentId = BaseContext.getCurrentId();
        PageHelper.startPage(page, pageSize);
        Page<ServiceReservation> p = (Page<ServiceReservation>) serviceReservationMapper.pageByStudentId(studentId);
        return new PageResult(p.getTotal(), p.getResult());
    }
}
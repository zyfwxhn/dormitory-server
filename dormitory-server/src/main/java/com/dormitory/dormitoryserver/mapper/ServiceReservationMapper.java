package com.dormitory.dormitoryserver.mapper;

import com.dormitory.dormitoryserver.entity.ServiceReservation;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Mapper
public interface ServiceReservationMapper {

    /**
     * 根据主键查询预约记录
     */
    ServiceReservation getById(Long id);

    /**
     * 更新预约状态
     */
    void updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 【定时任务专用】批量自动完成已过期的预约单
     */
    int autoCompleteExpiredReservations();

    /**
     * 【定时任务专用】查询即将被标记完成的过期预约
     */
    List<ServiceReservation> getExpiredReservations();

    /**
     * 【定时任务专用】查询即将开始的预约 (5-15分钟内), 用于开始前提醒
     */
    List<ServiceReservation> getUpcomingReservations();

    /**
     * 插入新的预约记录
     */
    void insert(ServiceReservation reservation);

    /**
     * 查询指定时间段内是否有冲突
     */
    int checkConflict(@Param("deviceId") Long deviceId,
                      @Param("date") LocalDate date,
                      @Param("startTime") LocalTime startTime,
                      @Param("endTime") LocalTime endTime);

    /**
     * 查询某设备在某天的所有有效预约
     */
    List<ServiceReservation> getValidReservationsByDeviceAndDate(@Param("deviceId") Long deviceId, @Param("date") LocalDate date);

    /**
     * 学生端分页查询自己的预约记录
     */
    Page<ServiceReservation> pageByStudentId(@Param("studentId") Long studentId);

    /**
     * 管理员端: 分页查询所有预约记录
     */
    Page<ServiceReservation> adminPageQuery(@Param("status") Integer status, @Param("studentNo") String studentNo);
}
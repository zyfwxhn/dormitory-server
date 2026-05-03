package com.dormitory.dormitoryserver.mapper;

import com.dormitory.dormitoryserver.entity.ServiceReservation;
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
     * 插入新的预约记录
     */
    void insert(ServiceReservation reservation);

    /**
     * 查询指定时间段内是否有冲突 (这个复杂的也可以顺便移进XML)
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
    List<ServiceReservation> pageByStudentId(@Param("studentId") Long studentId);
}
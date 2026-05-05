package com.dormitory.dormitoryserver.service;

import com.dormitory.dormitoryserver.dto.ReservationSubmitDTO;
import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.vo.AvailableTimeSlotVO;

import java.time.LocalDate;
import java.util.List;

public interface ServiceReservationService {

    /**
     * 学生提交生活服务预约
     * @param dto 预约参数
     */
    void submitReservation(ReservationSubmitDTO dto);

    List<AvailableTimeSlotVO> getAvailableSlots(Long deviceId, LocalDate reservationDate);

    /**
     * 学生取消预约
     * @param id 预约单ID
     */
    void cancelReservation(Long id);

    /**
     * 学生端分页查询自己的预约记录
     */
    PageResult pageQuery(Integer page, Integer pageSize);

    /**
     * 管理员端：分页查询所有预约记录
     */
    PageResult adminPageQuery(Integer page, Integer pageSize, Integer status, String studentNo);
}
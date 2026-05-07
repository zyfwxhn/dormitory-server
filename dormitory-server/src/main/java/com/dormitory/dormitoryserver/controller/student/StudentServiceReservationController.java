package com.dormitory.dormitoryserver.controller.student;

import com.dormitory.dormitoryserver.dto.ReservationSubmitDTO;
import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.result.Result;
import com.dormitory.dormitoryserver.service.ServiceReservationService;
import com.dormitory.dormitoryserver.vo.AvailableTimeSlotVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 学生端: 生活服务预约接口
 */
@RestController
@RequestMapping("/student/reservation")
@Slf4j
public class StudentServiceReservationController {

    @Autowired
    private ServiceReservationService serviceReservationService;

    /**
     * 提交洗衣机等服务预约
     * @param dto 前端传入的预约参数(JSON)
     * @return 统一响应
     */
    @PostMapping("/submit")
    public Result submit(@RequestBody @Validated ReservationSubmitDTO dto) {
        log.info("学生发起预约: {}", dto);

        serviceReservationService.submitReservation(dto);

        return Result.success();
    }

    /**
     * 查询洗衣机某天的可用空闲时段 (错峰推荐)
     */
    @GetMapping("/available-slots")
    public Result<List<AvailableTimeSlotVO>> getAvailableSlots(
            @RequestParam Long deviceId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate reservationDate) {

        List<AvailableTimeSlotVO> list = serviceReservationService.getAvailableSlots(deviceId, reservationDate);
        return Result.success(list);
    }

    /**
     * 查询当前学生的预约记录 (分页)
     */
    @GetMapping("/my")
    public Result<PageResult> pageQuery(@RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("学生查询自己的预约记录: page={}, pageSize={}", page, pageSize);
        PageResult pageResult = serviceReservationService.pageQuery(page, pageSize);
        return Result.success(pageResult);
    }

    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable Long id) {
        log.info("取消预约: {}", id);
        serviceReservationService.cancelReservation(id);
        return Result.success();
    }
}
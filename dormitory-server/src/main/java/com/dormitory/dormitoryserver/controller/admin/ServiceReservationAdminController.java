package com.dormitory.dormitoryserver.controller.admin;

import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.result.Result;
import com.dormitory.dormitoryserver.service.ServiceReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端：生活服务预约管理
 */
@RestController
@RequestMapping("/admin/reservation")
@Slf4j
public class ServiceReservationAdminController {

    @Autowired
    private ServiceReservationService serviceReservationService;

    @GetMapping("/page")
    public Result<PageResult> page(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) Integer status,
                                   @RequestParam(required = false) String studentNo) {
        log.info("管理员查询预约列表: page={}, pageSize={}, status={}, studentNo={}", page, pageSize, status, studentNo);
        PageResult result = serviceReservationService.adminPageQuery(page, pageSize, status, studentNo);
        return Result.success(result);
    }
}

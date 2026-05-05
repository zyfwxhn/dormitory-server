package com.dormitory.dormitoryserver.controller.student;

import com.dormitory.dormitoryserver.dto.DeviceResourcePageQueryDTO;
import com.dormitory.dormitoryserver.result.Result;
import com.dormitory.dormitoryserver.service.DeviceResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生端：设备资源查询
 */
@RestController
@RequestMapping("/student/device")
@Slf4j
public class StudentDeviceController {

    @Autowired
    private DeviceResourceService deviceResourceService;

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) String buildingNo) {
        DeviceResourcePageQueryDTO dto = new DeviceResourcePageQueryDTO();
        dto.setPage(1);
        dto.setPageSize(100);
        dto.setStatus(1); // 只返回可用设备
        dto.setBuildingNo(buildingNo); // 可选：按楼栋过滤
        log.info("学生查询可用设备列表, buildingNo={}", buildingNo);
        return Result.success(deviceResourceService.pageQuery(dto).getRecords());
    }
}

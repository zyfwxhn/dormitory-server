package com.dormitory.dormitoryserver.controller.admin;

import com.dormitory.dormitoryserver.dto.RepairOrderAdminPageQueryDTO;
import com.dormitory.dormitoryserver.dto.RepairOrderUpdateStatusDTO;
import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.result.Result;
import com.dormitory.dormitoryserver.service.RepairOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端/维修员端：报修订单管理接口
 */
@RestController
@RequestMapping("/admin/repair")
@Slf4j
@Validated
public class RepairOrderAdminController {

    @Autowired
    private RepairOrderService repairOrderService;

    /**
     * 管理端分页查询报修订单
     * @param queryDTO 动态组合查询条件
     * @return 统一响应体封装的分页结果
     */
    @GetMapping("/page")
    public Result<PageResult> pageQuery(RepairOrderAdminPageQueryDTO queryDTO) {
        log.info("管理端分页查询报修单参数：{}", queryDTO);

        // 调用 Service 层核心逻辑
        PageResult pageResult = repairOrderService.adminPageQuery(queryDTO);

        // 包装为全局统一响应格式返回给 Vue3
        return Result.success(pageResult);
    }

    /**
     * 智能派单接口
     * @param id 报修单ID
     * @return 统一响应
     */
    @PostMapping("/dispatch/{id}")
    public Result dispatch(@PathVariable Long id) {
        log.info("管理员触发智能派单，订单ID：{}", id);
        repairOrderService.autoDispatch(id);
        return Result.success("智能派单成功！");
    }
}
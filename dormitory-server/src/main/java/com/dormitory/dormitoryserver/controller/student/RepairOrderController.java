package com.dormitory.dormitoryserver.controller.student;

import com.dormitory.dormitoryserver.dto.RepairOrderEvaluationDTO;
import com.dormitory.dormitoryserver.dto.RepairOrderPageQueryDTO;
import com.dormitory.dormitoryserver.dto.RepairOrderSubmitDTO;
import com.dormitory.dormitoryserver.entity.RepairOrder;
import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.result.Result;
import com.dormitory.dormitoryserver.service.RepairOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student/repair")
@Slf4j
@Validated
public class RepairOrderController {

    @Autowired
    private RepairOrderService repairOrderService;

    @PostMapping("/submit")
    public Result submit(@RequestBody @Validated RepairOrderSubmitDTO dto) {
        log.info("学生提交报修订单: {}", dto);
        repairOrderService.submitOrder(dto);
        return Result.success();
    }

    /**
     * 历史报修订单分页查询
     * * @param queryDTO 分页和条件参数
     * @return 统一响应体封装的分页结果
     */
    @GetMapping("/page")
    public Result<PageResult> pageQuery(RepairOrderPageQueryDTO queryDTO) {
        log.info("学生端分页查询历史报修单: {}", queryDTO);

        
        PageResult pageResult = repairOrderService.pageQuery(queryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询报修单详情
     * * @param id 订单主键ID
     * @return 统一响应体封装的订单详情
     */
    @GetMapping("/{id}")
    public Result<RepairOrder> getDetail(@PathVariable("id") Long id) {
        log.info("学生端查询报修单详情: {}", id);

        RepairOrder repairOrder = repairOrderService.getDetail(id);

        return Result.success(repairOrder);
    }

    /**
     * 学生对已完成的报修单进行评价
     */
    @PostMapping("/evaluate")
    public Result<String> evaluate(@RequestBody @Validated RepairOrderEvaluationDTO repairOrderEvaluationDTO) {
        
         repairOrderService.evaluate(repairOrderEvaluationDTO);
        return Result.success();
    }

    /**
     * 新增: 取消报修单接口
     */
    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable("id") Long id) {
        log.info("学生取消报修订单: {}", id);
        repairOrderService.cancelOrder(id);
        return Result.success();
    }
}
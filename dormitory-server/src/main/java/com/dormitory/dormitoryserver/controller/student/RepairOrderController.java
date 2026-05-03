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
        log.info("学生提交报修订单：{}", dto);
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
        log.info("学生端分页查询历史报修单：{}", queryDTO);

        // 1. 调用 Service 层处理核心业务
        PageResult pageResult = repairOrderService.pageQuery(queryDTO);

        // 2. 封装进咱们的全局统一 Result 对象中返回给前端
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询报修单详情
     * 注意：使用 {id} 配合 @PathVariable 获取路径参数
     * * @param id 订单主键ID
     * @return 统一响应体封装的订单详情
     */
    @GetMapping("/{id}")
    public Result<RepairOrder> getDetail(@PathVariable("id") Long id) {
        log.info("学生端查询报修单详情：{}", id);

        RepairOrder repairOrder = repairOrderService.getDetail(id);

        return Result.success(repairOrder);
    }

    /**
     * 学生对已完成的报修单进行评价
     * 使用 @Validated 开启对 DTO 中校验注解的生效拦截
     */
    @PostMapping("/evaluate")
    public Result<String> evaluate(@RequestBody @Validated RepairOrderEvaluationDTO repairOrderEvaluationDTO) {
        //调用 Service 层进行业务校验与数据库更新
         repairOrderService.evaluate(repairOrderEvaluationDTO);
        return Result.success();
    }

    // 新增：取消报修单接口
    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable("id") Long id) {
        log.info("学生取消报修订单：{}", id);
        repairOrderService.cancelOrder(id);
        return Result.success();
    }
}
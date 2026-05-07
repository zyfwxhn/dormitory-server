package com.dormitory.dormitoryserver.controller.worker;

import com.dormitory.dormitoryserver.dto.RepairOrderPageQueryDTO;
import com.dormitory.dormitoryserver.dto.RepairOrderUpdateStatusDTO;
import com.dormitory.dormitoryserver.entity.RepairOrder;
import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.result.Result;
import com.dormitory.dormitoryserver.service.RepairOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 维修员端: 报修订单业务接口
 */
@RestController
@RequestMapping("/worker/repair")
@Slf4j
@Validated
public class RepairOrderWorkerController {

    @Autowired
    private RepairOrderService repairOrderService;

    /**
     * 维修员更新报修单状态 (接单、完成)
     * @param dto 包含订单id和目标状态
     * @return 统一无数据响应
     */
    @PutMapping("/status")
    public Result updateStatus(@RequestBody @Validated RepairOrderUpdateStatusDTO dto) {
        log.info("维修员端更新订单状态: {}", dto);

        repairOrderService.updateStatus(dto);

        return Result.success();
    }

    /**
     * 查询报修单详情
     */
    @GetMapping("/{id}")
    public Result<RepairOrder> getDetail(@PathVariable Long id) {
        log.info("维修员查询报修单详情: {}", id);
        return Result.success(repairOrderService.getDetailById(id));
    }

    /**
     * 维修员分页查询报修单
     * @param dto 包含页码、页大小、以及目标状态
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<PageResult> pageQuery(RepairOrderPageQueryDTO dto) {
        log.info("维修员分页查询报修单: {}", dto);

        PageResult pageResult = repairOrderService.workerPageQuery(dto);

        return Result.success(pageResult);
    }
}
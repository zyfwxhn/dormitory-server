package com.dormitory.dormitoryserver.controller.admin;

import com.dormitory.dormitoryserver.dto.SecondhandItemPageQueryDTO;
import com.dormitory.dormitoryserver.dto.ViolationReviewDTO;
import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.result.Result;
import com.dormitory.dormitoryserver.service.SecondhandItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/secondhand-item")
@Slf4j
public class SecondhandItemAdminController {

    @Autowired
    private SecondhandItemService secondhandItemService;

    @GetMapping("/page")
    public Result<PageResult> page(SecondhandItemPageQueryDTO dto) {
        log.info("管理员查询二手商品列表");
        return Result.success(secondhandItemService.pageQuery(dto));
    }

    @PutMapping("/violate")
    public Result violate(@RequestBody @Validated ViolationReviewDTO dto) {
        log.info("管理员强制下架二手商品：{}", dto);
        secondhandItemService.violate(dto);
        return Result.success("该商品已成功违规下架");
    }
}
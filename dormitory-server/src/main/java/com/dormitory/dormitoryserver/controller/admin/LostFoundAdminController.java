package com.dormitory.dormitoryserver.controller.admin;

import com.dormitory.dormitoryserver.dto.LostFoundPageQueryDTO;
import com.dormitory.dormitoryserver.dto.ViolationReviewDTO;
import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.result.Result;
import com.dormitory.dormitoryserver.service.LostFoundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/lost-found")
@Slf4j
public class LostFoundAdminController {

    @Autowired
    private LostFoundService lostFoundService;

    @GetMapping("/page")
    public Result<PageResult> page(LostFoundPageQueryDTO dto) {
        log.info("管理员查询失物招领列表: type={}, status={}, category={}", dto.getType(), dto.getStatus(), dto.getCategory());
        return Result.success(lostFoundService.adminPageQuery(dto));
    }

    @PutMapping("/violate")
    public Result violate(@RequestBody @Validated ViolationReviewDTO dto) {
        log.info("管理员强制下架失物招领信息: {}", dto);
        lostFoundService.violate(dto);
        return Result.success("该信息已成功违规下架");
    }
}
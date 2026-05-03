package com.dormitory.dormitoryserver.controller.admin;

import com.alibaba.excel.EasyExcel;
import com.dormitory.dormitoryserver.dto.WorkerExcelDTO;
import com.dormitory.dormitoryserver.dto.WorkerSaveDTO;
import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.result.Result;
import com.dormitory.dormitoryserver.service.WorkerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/admin/worker")
@Slf4j
@Validated
public class WorkerAdminController {

    @Autowired
    private WorkerService workerService;

    /**
     * 分页查询维修员列表
     */
    @GetMapping("/page")
    public Result<PageResult> pageQuery(WorkerSaveDTO dto) {
        log.info("管理端分页查询维修员列表：{}", dto);
        PageResult pageResult = workerService.pageQuery(dto);
        return Result.success(pageResult);
    }

    /**
     * 新增维修员
     */
    @PostMapping
    public Result save(@RequestBody WorkerSaveDTO dto) {
        log.info("新增维修员：{}", dto);
        workerService.save(dto);
        return Result.success();
    }

    /**
     * 修改维修员信息
     */
    @PutMapping
    public Result update(@RequestBody WorkerSaveDTO dto) {
        log.info("修改维修员信息：{}", dto);
        workerService.update(dto);
        return Result.success();
    }

    /**
     * 启用/停用维修员
     */
    @PutMapping("/{id}/status/{status}")
    public Result startOrStop(@PathVariable Long id, @PathVariable Integer status) {
        log.info("启用停用维修员：id={}, status={}", id, status);
        workerService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 批量导入维修员（Excel）
     */
    @PostMapping("/import")
    public Result importWorkers(@RequestParam("file") MultipartFile file) {
        log.info("管理员请求批量导入维修员：{}", file.getOriginalFilename());
        try {
            List<WorkerExcelDTO> list = EasyExcel.read(file.getInputStream())
                    .head(WorkerExcelDTO.class).sheet().doReadSync();
            workerService.importWorkerList(list);
            return Result.success("批量导入维修员成功！共导入 " + list.size() + " 条");
        } catch (IOException e) {
            log.error("Excel读取失败", e);
            return Result.error("导入失败：" + e.getMessage());
        }
    }
}

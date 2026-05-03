package com.dormitory.dormitoryserver.controller.admin;

import com.alibaba.excel.EasyExcel;
import com.dormitory.dormitoryserver.dto.DeviceExcelDTO;
import com.dormitory.dormitoryserver.dto.DeviceResourceDTO;
import com.dormitory.dormitoryserver.dto.DeviceResourcePageQueryDTO;
import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.result.Result;
import com.dormitory.dormitoryserver.service.DeviceResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 管理员端 - 设备资源管理接口
 */
@RestController
@RequestMapping("/admin/device")
@Slf4j
@Validated
public class DeviceResourceAdminController {

    @Autowired
    private DeviceResourceService deviceResourceService;

    /**
     * 设备资源分页查询
     * @param pageQueryDTO 分页查询参数对象
     * @return 统一返回结果封装类
     */
    @GetMapping("/page")
    public Result<PageResult> page(DeviceResourcePageQueryDTO pageQueryDTO) {
        log.info("管理员端设备分页查询，参数：{}", pageQueryDTO);
        PageResult pageResult = deviceResourceService.pageQuery(pageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 新增设备资源
     * @param deviceResourceDTO 设备信息 DTO
     * @return 统一返回成功结果
     */
    @PostMapping
    public Result save(@RequestBody @Validated DeviceResourceDTO deviceResourceDTO) {
        log.info("管理员端新增设备资源：{}", deviceResourceDTO);
        deviceResourceService.save(deviceResourceDTO);
        return Result.success();
    }

    /**
     * 修改设备信息
     * @param deviceResourceDTO 设备信息 DTO
     * @return 统一返回成功结果
     */
    @PutMapping
    public Result update(@RequestBody @Validated DeviceResourceDTO deviceResourceDTO) {
        log.info("管理员端修改设备信息：{}", deviceResourceDTO);
        deviceResourceService.update(deviceResourceDTO);
        return Result.success();
    }

    /**
     * 批量删除设备资源
     * @param ids 待删除的 ID 列表
     * @return 统一返回成功结果
     */
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids) {
        log.info("管理员端批量删除设备，IDs：{}", ids);
        deviceResourceService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 启用或禁用设备状态
     * @param status 目标状态（1启用，0禁用）
     * @param id 设备主键 ID
     * @return 统一返回成功结果
     */
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("管理员端切换设备状态：id={}, status={}", id, status);
        deviceResourceService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 批量导入设备（Excel）
     */
    @PostMapping("/import")
    public Result importDevices(@RequestParam("file") MultipartFile file) {
        log.info("管理员请求批量导入设备：{}", file.getOriginalFilename());
        try {
            List<DeviceExcelDTO> list = EasyExcel.read(file.getInputStream())
                    .head(DeviceExcelDTO.class).sheet().doReadSync();
            deviceResourceService.importDeviceList(list);
            return Result.success("批量导入设备成功！共导入 " + list.size() + " 条");
        } catch (IOException e) {
            log.error("Excel读取失败", e);
            return Result.error("导入失败：" + e.getMessage());
        }
    }
}
package com.dormitory.dormitoryserver.service.impl;

import com.dormitory.dormitoryserver.constant.StatusConstant;
import com.dormitory.dormitoryserver.dto.DeviceExcelDTO;
import com.dormitory.dormitoryserver.dto.DeviceResourceDTO;
import com.dormitory.dormitoryserver.dto.DeviceResourcePageQueryDTO;
import com.dormitory.dormitoryserver.entity.DeviceResource;
import com.dormitory.dormitoryserver.mapper.DeviceResourceMapper;
import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.service.DeviceResourceService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DeviceResourceServiceImpl implements DeviceResourceService {

    @Autowired
    private DeviceResourceMapper deviceResourceMapper;

    @Override
    public PageResult pageQuery(DeviceResourcePageQueryDTO pageQueryDTO) {
        log.info("设备资源分页查询：{}", pageQueryDTO);
        // 使用 PageHelper 开始分页
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());

        // 紧跟着的第一个 select 查询会被自动拦截并分页
        Page<DeviceResource> page = deviceResourceMapper.pageQuery(pageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void save(DeviceResourceDTO deviceResourceDTO) {
        log.info("新增设备资源：{}", deviceResourceDTO);
        DeviceResource deviceResource = new DeviceResource();

        // 对象属性拷贝 (将 DTO 拷贝到 Entity)
        BeanUtils.copyProperties(deviceResourceDTO, deviceResource);

        // 如果前端没传状态，默认设置为正常可用 (消灭魔法值！)
        if(deviceResource.getStatus() == null) {
            deviceResource.setStatus(StatusConstant.ENABLE);
        }

        // 设置时间
        deviceResource.setCreateTime(LocalDateTime.now());
        deviceResource.setUpdateTime(LocalDateTime.now());

        deviceResourceMapper.insert(deviceResource);
    }

    @Override
    public void update(DeviceResourceDTO deviceResourceDTO) {
        log.info("修改设备资源：{}", deviceResourceDTO);
        DeviceResource deviceResource = new DeviceResource();
        BeanUtils.copyProperties(deviceResourceDTO, deviceResource);
        deviceResource.setUpdateTime(LocalDateTime.now());

        deviceResourceMapper.update(deviceResource);
    }

    @Override
    public void deleteBatch(List<Long> ids) {
        log.info("批量删除设备资源：{}", ids);
        if (ids != null && !ids.isEmpty()) {
            deviceResourceMapper.deleteBatch(ids);
        }
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        log.info("启用禁用设备：id={}, status={}", id, status);
        // 构造实体对象
        DeviceResource deviceResource = DeviceResource.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .build();

        // 复用通用的 update 方法
        deviceResourceMapper.update(deviceResource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importDeviceList(List<DeviceExcelDTO> list) {
        if (list == null || list.isEmpty()) return;
        List<DeviceResource> devices = list.stream().map(dto -> {
            DeviceResource d = new DeviceResource();
            d.setBuildingNo(dto.getBuildingNo());
            d.setDeviceName(dto.getDeviceName());
            d.setStatus(StatusConstant.ENABLE);
            d.setCreateTime(LocalDateTime.now());
            d.setUpdateTime(LocalDateTime.now());
            return d;
        }).collect(Collectors.toList());
        deviceResourceMapper.insertBatch(devices);
        log.info("批量导入 {} 条设备成功", devices.size());
    }
}
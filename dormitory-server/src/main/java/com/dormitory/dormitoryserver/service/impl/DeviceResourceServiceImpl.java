package com.dormitory.dormitoryserver.service.impl;

import com.dormitory.dormitoryserver.constant.StatusConstant;
import com.dormitory.dormitoryserver.dto.DeviceExcelDTO;
import com.dormitory.dormitoryserver.dto.DeviceResourceDTO;
import com.dormitory.dormitoryserver.dto.DeviceResourcePageQueryDTO;
import com.dormitory.dormitoryserver.entity.DeviceResource;
import com.dormitory.dormitoryserver.mapper.DeviceResourceMapper;
import com.dormitory.dormitoryserver.result.PageResult;
import com.dormitory.dormitoryserver.service.DeviceResourceService;
import com.dormitory.dormitoryserver.websocket.WebSocketServer;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DeviceResourceServiceImpl implements DeviceResourceService {

    @Autowired
    private DeviceResourceMapper deviceResourceMapper;

    @Autowired
    private WebSocketServer webSocketServer;

    private static final Map<String, String> CN_NUM = Map.of(
        "一","1","二","2","三","3","四","4","五","5",
        "六","6","七","7","八","8","九","9","十","10"
    );

    @Override
    public PageResult pageQuery(DeviceResourcePageQueryDTO pageQueryDTO) {
        // 归一化 buildingNo：去掉"号楼"后缀，中文数字转阿拉伯数字
        String raw = pageQueryDTO.getBuildingNo();
        if (raw != null && !raw.isEmpty()) {
            raw = raw.replace("号楼", "");
            for (Map.Entry<String, String> e : CN_NUM.entrySet()) {
                raw = raw.replace(e.getKey(), e.getValue());
            }
            pageQueryDTO.setBuildingNo(raw);
        }
        log.info("设备资源分页查询：{}", pageQueryDTO);
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
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
        DeviceResource deviceResource = DeviceResource.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .build();
        deviceResourceMapper.update(deviceResource);
        // 广播设备状态变更，通知所有在线学生刷新设备列表
        webSocketServer.sendToAllClient("{\"type\":\"device_status_changed\"}");
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
package com.dormitory.dormitoryserver.service;

import com.dormitory.dormitoryserver.dto.DeviceExcelDTO;
import com.dormitory.dormitoryserver.dto.DeviceResourceDTO;
import com.dormitory.dormitoryserver.dto.DeviceResourcePageQueryDTO;
import com.dormitory.dormitoryserver.result.PageResult;
import java.util.List;

public interface DeviceResourceService {

    PageResult pageQuery(DeviceResourcePageQueryDTO pageQueryDTO);

    void save(DeviceResourceDTO deviceResourceDTO);

    void update(DeviceResourceDTO deviceResourceDTO);

    void deleteBatch(List<Long> ids);

    void startOrStop(Integer status, Long id);

    /**
     * 批量导入设备（Excel）
     */
    void importDeviceList(List<DeviceExcelDTO> list);
}
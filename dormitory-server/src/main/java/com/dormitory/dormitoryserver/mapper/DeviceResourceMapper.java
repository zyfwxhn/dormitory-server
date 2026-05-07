package com.dormitory.dormitoryserver.mapper;

import com.dormitory.dormitoryserver.dto.DeviceResourcePageQueryDTO;
import com.dormitory.dormitoryserver.entity.DeviceResource;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface DeviceResourceMapper {

    /**
     * 分页条件查询
     */
    Page<DeviceResource> pageQuery(DeviceResourcePageQueryDTO deviceResourcePageQueryDTO);

    /**
     * 新增设备资源
     */
    @Insert("insert into device_resource (building_no, device_name, status, create_time, update_time) " +
            "values (#{buildingNo}, #{deviceName}, #{status}, #{createTime}, #{updateTime})")
    void insert(DeviceResource deviceResource);

    /**
     * 动态修改设备信息
     */
    void update(DeviceResource deviceResource);

    /**
     * 根据主键批量删除设备
     */
    void deleteBatch(List<Long> ids);

    /**
     * 批量新增设备 (Excel 导入)
     */
    void insertBatch(List<DeviceResource> list);

    /**
     * 根据设备状态统计数量
     */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(id) FROM device_resource WHERE status = #{status}")
    Integer countByStatus(Integer status);
}
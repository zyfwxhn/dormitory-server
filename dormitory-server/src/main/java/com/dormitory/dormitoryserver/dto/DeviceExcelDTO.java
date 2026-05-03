package com.dormitory.dormitoryserver.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class DeviceExcelDTO {

    @ExcelProperty("楼栋号")
    private String buildingNo;

    @ExcelProperty("设备名称")
    private String deviceName;
}

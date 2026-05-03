package com.dormitory.dormitoryserver.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 学生 Excel 导入数据模型
 */
@Data
public class StudentExcelDTO {

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("楼栋号")
    private String buildingNo;

    @ExcelProperty("宿舍号")
    private String roomNo;

}
package com.dormitory.dormitoryserver.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class WorkerExcelDTO {

    @ExcelProperty("工号")
    private String username;

    @ExcelProperty("密码")
    private String password;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("技能")
    private String skills;
}

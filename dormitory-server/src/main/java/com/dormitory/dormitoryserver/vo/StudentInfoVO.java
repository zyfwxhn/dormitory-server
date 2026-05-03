package com.dormitory.dormitoryserver.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 学生个人信息展示 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentInfoVO implements Serializable {
    private Long id;
    private String studentNo;  // 学号
    private String name;       // 姓名
    private String gender;     // 性别
    private String grade;      // 年级
    private String phone;      // 手机号
    private String buildingNo; // 楼栋号
    private String roomNo;     // 宿舍号
    private String avatar;     // 头像
}
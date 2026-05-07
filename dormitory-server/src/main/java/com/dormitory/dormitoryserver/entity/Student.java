package com.dormitory.dormitoryserver.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学生实体类 (对应数据库 student 表)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;                // 主键, 自增

    private String studentNo;       // 学号

    private String password;        // 密码

    private String name;            // 姓名

    private String gender;          // 性别 (0:女, 1:男)

    private String grade;           // 年级

    private String phone;           // 手机号

    private String buildingNo;      // 楼栋号

    private String roomNo;          // 宿舍号

    private String avatar;          // 头像链接

    private LocalDateTime createTime; // 记录创建时间

    private LocalDateTime updateTime; // 记录最后修改时间
}
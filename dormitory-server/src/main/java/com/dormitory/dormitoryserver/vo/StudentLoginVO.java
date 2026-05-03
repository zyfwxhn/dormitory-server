package com.dormitory.dormitoryserver.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 学生登录响应结果 VO
 * 用于封装后端返回给前端的数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentLoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;       // 学生ID（主键）

    private String name;   // 学生姓名（用于前端展示“欢迎您，XXX”）

    private String token;  // JWT 身份令牌（前端后续请求必须携带此 token）

}
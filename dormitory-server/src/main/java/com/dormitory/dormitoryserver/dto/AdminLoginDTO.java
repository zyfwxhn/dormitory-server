package com.dormitory.dormitoryserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serializable;

@Data
public class AdminLoginDTO implements Serializable {

    @NotBlank(message = "管理员账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
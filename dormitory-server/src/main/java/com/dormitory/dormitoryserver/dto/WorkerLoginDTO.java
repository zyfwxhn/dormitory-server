package com.dormitory.dormitoryserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serializable;

@Data
public class WorkerLoginDTO implements Serializable {

    @NotBlank(message = "工号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
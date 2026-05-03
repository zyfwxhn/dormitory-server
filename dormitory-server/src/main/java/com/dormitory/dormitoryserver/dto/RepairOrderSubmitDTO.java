package com.dormitory.dormitoryserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serializable;

@Data
public class RepairOrderSubmitDTO implements Serializable {

    @NotBlank(message = "故障类型不能为空")
    private String repairType;

    @NotBlank(message = "故障描述不能为空")
    private String description;

    private String images;
}
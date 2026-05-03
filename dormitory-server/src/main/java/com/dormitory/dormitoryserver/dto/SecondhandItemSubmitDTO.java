package com.dormitory.dormitoryserver.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SecondhandItemSubmitDTO {

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 64, message = "商品名称最长为64个字符")
    private String name;

    @NotBlank(message = "商品详细描述不能为空")
    private String description;

    @NotBlank(message = "商品分类不能为空")
    private String category;

    @NotNull(message = "出售价格不能为空")
    @DecimalMin(value = "0.00", message = "商品价格不能为负数")
    private BigDecimal price;

    @NotBlank(message = "商品成色不能为空")
    private String conditionLevel;

    private String images;
}
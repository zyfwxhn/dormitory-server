package com.dormitory.dormitoryserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LostFoundSubmitDTO {

    @NotNull(message = "信息类型不能为空")
    // 0: 寻物启事, 1: 失物招领
    private Integer type;

    @NotBlank(message = "标题不能为空")
    @Size(max = 64, message = "标题最长为64个字符")
    private String title;

    @NotBlank(message = "物品描述不能为空")
    private String description;

    @NotBlank(message = "物品分类不能为空")
    private String category;

    // 地点非必填，可以为空
    private String location;

    @NotBlank(message = "联系方式不能为空")
    private String contactInfo;

    // 图片也是非必填的（有些丢东西的人可能没有拍照片）
    private String images;
}
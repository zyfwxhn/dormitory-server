package com.dormitory.dormitoryserver.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RepairOrderEvaluationDTO {

    // 必须指定是对哪一个单子进行评价
    @NotNull(message = "报修单ID不能为空")
    private Long id;

    // 评分范围严格限制在 1-5 之间
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1分")
    @Max(value = 5, message = "评分最高为5分")
    private Integer evaluationScore;

    // 评价内容不是必填项，但如果填了，限制长度防止恶意灌水或数据库字段超长
    @Size(max = 255, message = "评价内容最多255个字符")
    private String evaluationContent;
}
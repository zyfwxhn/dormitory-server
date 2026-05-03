package com.dormitory.dormitoryserver.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 封装分页查询结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult implements Serializable {

    // 总记录数
    private long total;

    // 当前页数据集合 (使用 <?> 消除 Unchecked 泛型警告，且不影响原有业务逻辑)
    private List<?> records;

}
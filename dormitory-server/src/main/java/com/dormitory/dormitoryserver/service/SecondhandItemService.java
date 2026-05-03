package com.dormitory.dormitoryserver.service;

import com.dormitory.dormitoryserver.dto.SecondhandItemPageQueryDTO;
import com.dormitory.dormitoryserver.dto.SecondhandItemSubmitDTO;
import com.dormitory.dormitoryserver.dto.SecondhandItemUpdateStatusDTO;
import com.dormitory.dormitoryserver.dto.ViolationReviewDTO;
import com.dormitory.dormitoryserver.entity.SecondhandItem;
import com.dormitory.dormitoryserver.result.PageResult;

public interface SecondhandItemService {
    /**
     * 发布二手商品
     * @param dto 前端提交的数据
     */
    void publish(SecondhandItemSubmitDTO dto);

    /**
     * 分页条件查询二手商品信息
     * @param dto 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(SecondhandItemPageQueryDTO dto);

    /**
     * 更新二手商品状态
     */
    void updateStatus(SecondhandItemUpdateStatusDTO dto);

    /**
     * 根据ID查询二手商品详情
     * @param id 商品ID
     * @return 商品实体
     */
    SecondhandItem getById(Long id);

    /**
     * 管理员违规下架二手商品
     */
    void violate(ViolationReviewDTO dto);
}
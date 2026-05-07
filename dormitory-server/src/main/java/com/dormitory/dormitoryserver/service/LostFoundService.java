package com.dormitory.dormitoryserver.service;

import com.dormitory.dormitoryserver.dto.LostFoundPageQueryDTO;
import com.dormitory.dormitoryserver.dto.LostFoundSubmitDTO;
import com.dormitory.dormitoryserver.dto.LostFoundUpdateStatusDTO;
import com.dormitory.dormitoryserver.dto.ViolationReviewDTO;
import com.dormitory.dormitoryserver.entity.LostFound;
import com.dormitory.dormitoryserver.result.PageResult;

public interface LostFoundService {
    /**
     * 发布寻物启事 / 失物招领
     * @param dto 前端提交的数据
     */
    void publish(LostFoundSubmitDTO dto);

    /**
     * 分页条件查询失物招领信息
     * @param dto 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(LostFoundPageQueryDTO dto);

    /**
     * 更新失物招领状态
     */
    void updateStatus(LostFoundUpdateStatusDTO dto);

    /**
     * 根据ID查询失物招领详情
     * @param id 记录ID
     * @return 失物招领实体
     */
    LostFound getById(Long id);

    /**
     * 管理员违规下架失物招领
     */
    void violate(ViolationReviewDTO dto);

    /**
     * 管理员分页查询
     */
    PageResult adminPageQuery(LostFoundPageQueryDTO dto);

    /**
     * 学生发起认领: 给发布者发送通知, 不暴露双方联系方式
     */
    void claim(Long lostFoundId);

    /**
     * 学生编辑自己的发布信息
     */
    void edit(LostFoundSubmitDTO dto, Long id);
}
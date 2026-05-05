package com.dormitory.dormitoryserver.service;

import com.dormitory.dormitoryserver.dto.*;
import com.dormitory.dormitoryserver.entity.RepairOrder;
import com.dormitory.dormitoryserver.result.PageResult;

public interface RepairOrderService {
    void submitOrder(RepairOrderSubmitDTO dto);

    /**
     * 历史报修订单分页查询
     * @param queryDTO 分页和条件参数
     * @return 统一分页结果
     */
    PageResult pageQuery(RepairOrderPageQueryDTO queryDTO);

    /**
     * 查询报修单详情
     * @param id 订单ID
     * @return 报修单详细信息
     */
    RepairOrder getDetail(Long id);

    /**
     * 管理端：历史报修订单动态分页查询
     * @param queryDTO 管理端分页和高级筛选条件
     * @return 统一分页结果
     */
    PageResult adminPageQuery(RepairOrderAdminPageQueryDTO queryDTO);

    /**
     * 维修员接单/完成维修 (状态流转)
     * @param dto 包含订单id和目标状态
     */
    void updateStatus(RepairOrderUpdateStatusDTO dto);

    /**
     * 学生评价已完成的报修单
     * @param repairOrderEvaluationDTO 评价数据传输对象
     */
    void evaluate(RepairOrderEvaluationDTO repairOrderEvaluationDTO);

    void cancelOrder(Long id);

    /**
     * 按ID查询报修单详情（管理员/维修员使用，不校验学生身份）
     */
    RepairOrder getDetailById(Long id);

    /**
     * 智能派单
     * @param orderId 报修单 ID
     */
    void autoDispatch(Long orderId);

    /**
     * 维修员分页查询报修单
     * @param dto
     * @return
     */
    PageResult workerPageQuery(RepairOrderPageQueryDTO dto);
}
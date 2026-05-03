package com.dormitory.dormitoryserver.constant;

/**
 * 业务状态常量类
 */
public class StatusConstant {

    // 报修单状态：0-待处理，1-已接单，2-维修中，3-已完成，4-已取消
    public static final Integer REPAIR_PENDING = 0;
    public static final Integer REPAIR_ACCEPTED = 1;
    public static final Integer REPAIR_PROCESSING = 2;
    public static final Integer REPAIR_FINISHED = 3;
    public static final Integer REPAIR_CANCELLED = 4;

    // 启用/禁用状态：1-启用，0-禁用
    public static final Integer ENABLE = 1;
    public static final Integer DISABLE = 0;
}
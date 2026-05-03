package com.dormitory.dormitoryserver.service;

import com.dormitory.dormitoryserver.vo.DataStatisticsVO;

/**
 * 数据统计大屏业务接口
 */
public interface DataStatisticsService {

    /**
     * 获取数据大屏总览聚合数据
     * @return 聚合 VO
     */
    DataStatisticsVO getOverviewData();

}
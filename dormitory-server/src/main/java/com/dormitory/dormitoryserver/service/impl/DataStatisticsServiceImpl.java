package com.dormitory.dormitoryserver.service.impl;

import com.dormitory.dormitoryserver.constant.StatusConstant;
import com.dormitory.dormitoryserver.mapper.DeviceResourceMapper;
import com.dormitory.dormitoryserver.mapper.RepairOrderMapper;
import com.dormitory.dormitoryserver.mapper.WorkerMapper;
import com.dormitory.dormitoryserver.service.DataStatisticsService;
import com.dormitory.dormitoryserver.vo.DataStatisticsVO;
import com.dormitory.dormitoryserver.vo.StatusCountVO;
import com.dormitory.dormitoryserver.vo.TodayMetricsVO;
import com.dormitory.dormitoryserver.vo.WorkerRankVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DataStatisticsServiceImpl implements DataStatisticsService {

    // 注入我们上一步写好的 3 个 Mapper
    @Autowired
    private RepairOrderMapper repairOrderMapper;
    @Autowired
    private DeviceResourceMapper deviceResourceMapper;
    @Autowired
    private WorkerMapper workerMapper;

    @Override
    public DataStatisticsVO getOverviewData() {
        log.info("开始聚合大屏统计数据...");

        // 1. 组装【今日指标看板】
        Integer newRepairCount = repairOrderMapper.getTodayNewRepairCount();
        Integer finishedRepairCount = repairOrderMapper.getTodayFinishedRepairCount();
        Integer idleDeviceCount = deviceResourceMapper.countByStatus(StatusConstant.ENABLE); // 假设1为启用/可用闲置状态

        // 严谨的防御性编程：防止数据库查出 null 导致包装类异常
        TodayMetricsVO todayMetrics = TodayMetricsVO.builder()
                .newRepairCount(newRepairCount == null ? 0 : newRepairCount)
                .finishedRepairCount(finishedRepairCount == null ? 0 : finishedRepairCount)
                .idleDeviceCount(idleDeviceCount == null ? 0 : idleDeviceCount)
                .build();

        // 2. 获取【状态分布饼图】
        List<StatusCountVO> statusProportion = repairOrderMapper.getStatusCount();

        // 3. 获取【维修员完单排行榜】
        List<WorkerRankVO> workerRanking = workerMapper.getWorkerRanking();

        // 4. 将所有零散数据装入巨型大碗（数据总览 VO）返回
        return DataStatisticsVO.builder()
                .todayMetrics(todayMetrics)
                .statusProportion(statusProportion)
                .workerRanking(workerRanking)
                .build();
    }
}
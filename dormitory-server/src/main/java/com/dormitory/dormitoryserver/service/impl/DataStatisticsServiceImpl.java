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
    @Autowired
    private RepairOrderMapper repairOrderMapper;
    @Autowired
    private DeviceResourceMapper deviceResourceMapper;
    @Autowired
    private WorkerMapper workerMapper;

    @Override
    public DataStatisticsVO getOverviewData() {
        log.info("开始聚合大屏统计数据...");
        Integer newRepairCount = repairOrderMapper.getTodayNewRepairCount();
        Integer finishedRepairCount = repairOrderMapper.getTodayFinishedRepairCount();
        Integer idleDeviceCount = deviceResourceMapper.countByStatus(StatusConstant.ENABLE); // 假设1为启用/可用闲置状态
        TodayMetricsVO todayMetrics = TodayMetricsVO.builder()
                .newRepairCount(newRepairCount == null ? 0 : newRepairCount)
                .finishedRepairCount(finishedRepairCount == null ? 0 : finishedRepairCount)
                .idleDeviceCount(idleDeviceCount == null ? 0 : idleDeviceCount)
                .build();
        List<StatusCountVO> statusProportion = repairOrderMapper.getStatusCount();
        List<WorkerRankVO> workerRanking = workerMapper.getWorkerRanking();

        // 组装数据总览VO
        return DataStatisticsVO.builder()
                .todayMetrics(todayMetrics)
                .statusProportion(statusProportion)
                .workerRanking(workerRanking)
                .build();
    }
}
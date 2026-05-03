package com.dormitory.dormitoryserver.controller.admin;

import com.dormitory.dormitoryserver.result.Result;
import com.dormitory.dormitoryserver.service.DataStatisticsService;
import com.dormitory.dormitoryserver.vo.DataStatisticsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员端 - 数据大屏统计接口
 */
@RestController
@RequestMapping("/admin/statistics")
@Slf4j
public class DataStatisticsAdminController {

    @Autowired
    private DataStatisticsService dataStatisticsService;

    /**
     * 获取大盘数据总览
     * @return 统一结果封装的大屏 VO 对象
     */
    @GetMapping("/overview")
    public Result<DataStatisticsVO> overview() {
        log.info("管理员获取数据大盘总览信息");
        DataStatisticsVO dataStatisticsVO = dataStatisticsService.getOverviewData();
        return Result.success(dataStatisticsVO);
    }
}
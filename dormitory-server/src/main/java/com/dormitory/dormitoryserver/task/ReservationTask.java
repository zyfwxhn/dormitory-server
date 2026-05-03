package com.dormitory.dormitoryserver.task;

import com.dormitory.dormitoryserver.mapper.ServiceReservationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 生活服务预约定时任务
 */
@Component
@Slf4j
public class ReservationTask {

    @Autowired
    private ServiceReservationMapper serviceReservationMapper;

    /**
     * 自动完成已过期的预约单
     * Cron 表达式："0 0/10 * * * ?" 表示每 10 分钟执行一次
     * (答辩时你可以说是每分钟/每小时执行，视业务需求而定)
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void autoCompleteExpired() {
        log.info("【定时任务触发】开始清理已过期的洗衣机/洗鞋机预约单...");

        // 调用 Mapper 层的批量更新 SQL
        int count = serviceReservationMapper.autoCompleteExpiredReservations();

        if (count > 0) {
            log.info("清理完毕，共自动将 {} 条过期预约单标记为已完成。", count);
        } else {
            log.info("目前没有需要清理的过期单据。");
        }
    }
}
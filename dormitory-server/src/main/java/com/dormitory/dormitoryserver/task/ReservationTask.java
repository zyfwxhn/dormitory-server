package com.dormitory.dormitoryserver.task;

import com.dormitory.dormitoryserver.entity.Notification;
import com.dormitory.dormitoryserver.entity.ServiceReservation;
import com.dormitory.dormitoryserver.mapper.NotificationMapper;
import com.dormitory.dormitoryserver.mapper.ServiceReservationMapper;
import com.dormitory.dormitoryserver.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 生活服务预约定时任务
 */
@Component
@Slf4j
public class ReservationTask {

    @Autowired
    private ServiceReservationMapper serviceReservationMapper;

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 自动完成已过期的预约单
     * 每 10 分钟执行一次
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void autoCompleteExpired() {
        log.info("【定时任务触发】开始清理已过期的预约单...");

        // 1. 先查出过期的预约，用于推送通知
        List<ServiceReservation> expiredList = serviceReservationMapper.getExpiredReservations();

        // 2. 批量更新
        int count = serviceReservationMapper.autoCompleteExpiredReservations();

        // 3. 持久化通知 + WebSocket 实时推送
        if (count > 0) {
            log.info("清理完毕，共自动将 {} 条过期预约单标记为已完成。", count);
            for (ServiceReservation r : expiredList) {
                try {
                    // 第一道防线：存入数据库（离线也能看到）
                    Notification noti = new Notification();
                    noti.setStudentId(r.getStudentId());
                    noti.setTitle("预约自动完成");
                    noti.setContent("您在 " + r.getReservationDate() + " " + r.getStartTime() + "-" + r.getEndTime() + " 的预约已自动完成。");
                    noti.setType(3); // 3: 预约提醒
                    noti.setIsRead(0);
                    noti.setCreateTime(LocalDateTime.now());
                    notificationMapper.insert(noti);

                    // 第二道防线：WebSocket 在线实时推送
                    String wsMsg = "{\"type\":\"reservation_completed\"}";
                    webSocketServer.sendToSpecificClient(r.getStudentId().toString(), wsMsg);
                } catch (Exception e) {
                    log.warn("处理过期预约通知失败, 预约ID={}: {}", r.getId(), e.getMessage());
                }
            }
        } else {
            log.info("目前没有需要清理的过期单据。");
        }
    }

    /**
     * 预约开始前提醒
     * 每 5 分钟执行一次，检查未来 15 分钟内即将开始的预约
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void sendStartReminders() {
        List<ServiceReservation> upcomingList = serviceReservationMapper.getUpcomingReservations();
        if (upcomingList.isEmpty()) return;

        log.info("【预约提醒】发现 {} 条即将开始的预约，发送提醒...", upcomingList.size());
        for (ServiceReservation r : upcomingList) {
            try {
                // 数据库持久化
                Notification noti = new Notification();
                noti.setStudentId(r.getStudentId());
                noti.setTitle("预约即将开始");
                noti.setContent("您预约的 " + r.getStartTime() + "-" + r.getEndTime() + " 时段即将开始，请准时前往！");
                noti.setType(3);
                noti.setIsRead(0);
                noti.setCreateTime(LocalDateTime.now());
                notificationMapper.insert(noti);

                // WebSocket 实时推送
                String wsMsg = "{\"type\":\"reservation_reminder\"}";
                webSocketServer.sendToSpecificClient(r.getStudentId().toString(), wsMsg);
            } catch (Exception e) {
                log.warn("发送预约提醒失败, 预约ID={}: {}", r.getId(), e.getMessage());
            }
        }
    }
}
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

@Component
@Slf4j
public class ReservationTask {

    @Autowired
    private ServiceReservationMapper serviceReservationMapper;

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private NotificationMapper notificationMapper;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void autoCompleteExpired() {
        log.info("开始清理已过期的预约单...");

        List<ServiceReservation> expiredList = serviceReservationMapper.getExpiredReservations();
        int count = serviceReservationMapper.autoCompleteExpiredReservations();

        if (count > 0) {
            log.info("已将 {} 条过期预约单标记为已完成", count);
            for (ServiceReservation r : expiredList) {
                try {
                    Notification noti = new Notification();
                    noti.setStudentId(r.getStudentId());
                    noti.setTitle("预约自动完成");
                    noti.setContent("您在 " + r.getReservationDate() + " " + r.getStartTime() + "-" + r.getEndTime() + " 的预约已自动完成.");
                    noti.setType(3);
                    noti.setIsRead(0);
                    noti.setCreateTime(LocalDateTime.now());
                    notificationMapper.insert(noti);

                    String wsMsg = "{\"type\":\"reservation_completed\"}";
                    webSocketServer.sendToSpecificClient(r.getStudentId().toString(), wsMsg);
                } catch (Exception e) {
                    log.warn("处理过期预约通知失败, 预约ID={}: {}", r.getId(), e.getMessage());
                }
            }
        } else {
            log.info("没有需要清理的过期预约单");
        }
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    public void sendStartReminders() {
        List<ServiceReservation> upcomingList = serviceReservationMapper.getUpcomingReservations();
        if (upcomingList.isEmpty()) return;

        log.info("发现 {} 条即将开始的预约", upcomingList.size());
        for (ServiceReservation r : upcomingList) {
            try {
                Notification noti = new Notification();
                noti.setStudentId(r.getStudentId());
                noti.setTitle("预约即将开始");
                noti.setContent("您预约的 " + r.getStartTime() + "-" + r.getEndTime() + " 时段即将开始, 请准时前往.");
                noti.setType(3);
                noti.setIsRead(0);
                noti.setCreateTime(LocalDateTime.now());
                notificationMapper.insert(noti);

                String wsMsg = "{\"type\":\"reservation_reminder\"}";
                webSocketServer.sendToSpecificClient(r.getStudentId().toString(), wsMsg);
            } catch (Exception e) {
                log.warn("发送预约提醒失败, 预约ID={}: {}", r.getId(), e.getMessage());
            }
        }
    }
}

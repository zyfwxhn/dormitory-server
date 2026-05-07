package com.dormitory.dormitoryserver.service;

import com.dormitory.dormitoryserver.entity.Notification;
import java.util.List;

public interface NotificationService {

    /**
     * 获取指定学生的系统通知
     * @param studentId
     * @return
     */
    List<Notification> getStudentNotifications(Long studentId);

    /**
     * 将通知标记为已读
     * @param id
     * @param studentId
     */
    void markAsRead(Long id, Long studentId);
}
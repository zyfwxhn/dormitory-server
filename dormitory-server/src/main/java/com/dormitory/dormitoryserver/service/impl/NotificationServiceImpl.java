package com.dormitory.dormitoryserver.service.impl;

import com.dormitory.dormitoryserver.entity.Notification;
import com.dormitory.dormitoryserver.mapper.NotificationMapper;
import com.dormitory.dormitoryserver.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public List<Notification> getStudentNotifications(Long studentId) {
        return notificationMapper.getByStudentId(studentId);
    }

    @Override
    public void markAsRead(Long id, Long studentId) {
        notificationMapper.markAsRead(id, studentId);
    }
}
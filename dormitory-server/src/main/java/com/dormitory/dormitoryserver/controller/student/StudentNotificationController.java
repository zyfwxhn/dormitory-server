package com.dormitory.dormitoryserver.controller.student;

import com.dormitory.dormitoryserver.context.BaseContext;
import com.dormitory.dormitoryserver.entity.Notification;
import com.dormitory.dormitoryserver.result.Result;
import com.dormitory.dormitoryserver.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生端 - 系统通知接口
 */
@RestController
@RequestMapping("/student/notification")
public class StudentNotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取当前登录学生的所有系统通知
     * @return
     */
    @GetMapping
    public Result<List<Notification>> getMyNotifications() {
        // 从 ThreadLocal 中获取当前登录学生的 ID
        Long currentStudentId = BaseContext.getCurrentId();
        List<Notification> notifications = notificationService.getStudentNotifications(currentStudentId);
        return Result.success(notifications);
    }

    /**
     * 将指定通知标记为已读
     * 这里使用 PUT 请求，代表修改状态
     * @param id
     * @return
     */
    @PutMapping("/{id}/read")
    public Result markAsRead(@PathVariable Long id) {
        Long currentStudentId = BaseContext.getCurrentId();
        notificationService.markAsRead(id, currentStudentId);
        return Result.success();
    }
}
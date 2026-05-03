package com.dormitory.dormitoryserver.mapper;

import com.dormitory.dormitoryserver.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface NotificationMapper {

    /**
     * 插入一条新系统通知
     * @param notification
     */
    void insert(Notification notification);

    /**
     * 根据学生ID查询其所有的通知列表（按时间降序）
     * @param studentId
     * @return
     */
    List<Notification> getByStudentId(Long studentId);

    /**
     * 将某条通知标记为已读（携带学生ID防止横向越权）
     * @param id
     * @param studentId
     */
    @Update("UPDATE notification SET is_read = 1 WHERE id = #{id} AND student_id = #{studentId}")
    void markAsRead(Long id, Long studentId);
}
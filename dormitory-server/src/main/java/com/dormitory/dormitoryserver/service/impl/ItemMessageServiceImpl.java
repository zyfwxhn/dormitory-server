package com.dormitory.dormitoryserver.service.impl;

import com.dormitory.dormitoryserver.entity.ItemMessage;
import com.dormitory.dormitoryserver.entity.Notification;
import com.dormitory.dormitoryserver.mapper.ItemMessageMapper;
import com.dormitory.dormitoryserver.mapper.NotificationMapper;
import com.dormitory.dormitoryserver.service.ItemMessageService;
import com.dormitory.dormitoryserver.vo.ItemMessageVO;
import com.dormitory.dormitoryserver.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ItemMessageServiceImpl implements ItemMessageService {

    @Autowired
    private ItemMessageMapper itemMessageMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    // 1. 注入我们刚刚写好的 WebSocket 组件
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * @Transactional 保证留言和发通知两个动作在同一个事务中
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishMessage(ItemMessage itemMessage) {
        // 1. 补全留言时间并保存留言
        itemMessage.setCreateTime(LocalDateTime.now());
        itemMessageMapper.insert(itemMessage);

        // 2. 构造一条系统通知发给接收方(卖家)
        Notification notification = new Notification();
        notification.setStudentId(itemMessage.getToStudentId());
        notification.setTitle("二手交易新留言提醒");

        // 截取部分留言内容作为通知摘要
        String contentPreview = itemMessage.getContent().length() > 20
                ? itemMessage.getContent().substring(0, 20) + "..."
                : itemMessage.getContent();

        String finalNotifyContent = "您的二手商品收到了新留言: " + contentPreview;
        notification.setContent(finalNotifyContent);
        notification.setType(2); // 业务类型：2-二手留言
        notification.setIsRead(0); // 0-未读
        notification.setCreateTime(LocalDateTime.now());

        // 3. 保存通知到数据库（离线持久化）
        notificationMapper.insert(notification);

        // 4. WebSocket 实时推送（JSON 格式，前端可按 type 区分处理）
        String wsMsg = String.format("{\"type\":\"new_message\",\"itemId\":%d}", itemMessage.getItemId());
        webSocketServer.sendToSpecificClient(
                itemMessage.getToStudentId().toString(),
                wsMsg
        );
    }

    @Override
    public List<ItemMessageVO> getMessagesByItemId(Long itemId) {
        return itemMessageMapper.getByItemId(itemId);
    }
}
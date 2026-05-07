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

    @Autowired
    private WebSocketServer webSocketServer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishMessage(ItemMessage itemMessage) {
        itemMessage.setCreateTime(LocalDateTime.now());
        itemMessageMapper.insert(itemMessage);

        // 给接收方发系统通知
        Notification notification = new Notification();
        notification.setStudentId(itemMessage.getToStudentId());
        notification.setTitle("二手交易新留言提醒");
        String contentPreview = itemMessage.getContent().length() > 20
                ? itemMessage.getContent().substring(0, 20) + "..."
                : itemMessage.getContent();
        notification.setContent("您的二手商品收到了新留言: " + contentPreview);
        notification.setType(2);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);

        // WebSocket 实时推送
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

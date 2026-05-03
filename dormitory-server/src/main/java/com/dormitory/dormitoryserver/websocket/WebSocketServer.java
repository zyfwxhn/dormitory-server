package com.dormitory.dormitoryserver.websocket;

import org.springframework.stereotype.Component;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 服务端点
 * 路径中的 {sid} 代表登录学生的 ID
 */
@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {

    // 存放会话对象，使用线程安全的 ConcurrentHashMap
    // Key: 学生ID (sid), Value: 对应的 WebSocket Session
    private static Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        System.out.println("客户端：" + sid + " 建立连接");
        sessionMap.put(sid, session);
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        System.out.println("收到来自客户端：" + sid + " 的信息: " + message);
        // 通常用作心跳检测等，我们目前主要依赖后端主动推送
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        System.out.println("连接断开:" + sid);
        sessionMap.remove(sid);
    }

    /**
     * 群发消息（可选扩展，比如发全局系统公告）
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                // 服务器向客户端发送消息
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送消息给指定客户端（核心方法：用于精准推送通知）
     * @param sid 学生ID
     * @param message 消息内容 (通常可以转为 JSON 字符串)
     */
    public void sendToSpecificClient(String sid, String message) {
        Session session = sessionMap.get(sid);
        if (session != null) {
            try {
                session.getBasicRemote().sendText(message);
                System.out.println("向客户端：" + sid + " 推送消息成功");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("客户端：" + sid + " 不在线，跳过推送");
        }
    }
}
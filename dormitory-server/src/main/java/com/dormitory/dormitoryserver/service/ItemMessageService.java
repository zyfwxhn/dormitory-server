package com.dormitory.dormitoryserver.service;

import com.dormitory.dormitoryserver.entity.ItemMessage;
import com.dormitory.dormitoryserver.vo.ItemMessageVO;
import java.util.List;

public interface ItemMessageService {

    void publishMessage(ItemMessage itemMessage);

    List<ItemMessageVO> getMessagesByItemId(Long itemId);
}
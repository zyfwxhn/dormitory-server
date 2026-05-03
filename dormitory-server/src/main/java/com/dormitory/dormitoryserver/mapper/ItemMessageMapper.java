package com.dormitory.dormitoryserver.mapper;

import com.dormitory.dormitoryserver.entity.ItemMessage;
import com.dormitory.dormitoryserver.vo.ItemMessageVO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ItemMessageMapper {

    void insert(ItemMessage itemMessage);

    List<ItemMessageVO> getByItemId(Long itemId);
}
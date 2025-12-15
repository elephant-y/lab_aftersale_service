package org.oomall.labservice.mapper;

import org.apache.ibatis.annotations.*;
import org.oomall.labservice.mapper.Po.ServiceOrderPo;

@Mapper
public interface ServiceOrderMapper  {
    @Select("SELECT COUNT(*) FROM service_order WHERE aftersale_id = #{aftersaleId}")
    int countByAftersaleId(@Param("aftersaleId") Long aftersaleId);

    @Insert("""
        INSERT INTO service_order(shop_id, aftersale_id, type, consignee)
        VALUES(#{shopId}, #{aftersaleId}, #{type}, #{consignee})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ServiceOrderPo serviceOrderPo);
}

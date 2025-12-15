package org.oomall.labaftersale.mapper;

import org.oomall.labaftersale.mapper.po.AftersalePo;


import org.apache.ibatis.annotations.*;

@Mapper
public interface AftersaleMapper {
     @Select("select * from aftersale where id = #{id} and shop_id = #{shopId}")
     AftersalePo findByIdAndShopId(Long id, Long shopId);
     @Insert("insert into aftersale (shop_id, type, status) values (#{shopId}, #{type}, #{status})")
    void save(AftersalePo po);
}

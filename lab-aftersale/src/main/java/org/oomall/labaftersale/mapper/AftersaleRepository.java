package org.oomall.labaftersale.mapper;

import org.oomall.labaftersale.mapper.po.AftersalePo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AftersaleRepository extends JpaRepository<AftersalePo, Long> {
    AftersalePo findByIdAndShopId(Long id, Long shopId);
    // 新增：通过order_item_id查询是否已存在售后单
    boolean existsByOrderItemId(Long orderItemId);
}

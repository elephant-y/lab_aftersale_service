package org.oomall.labaftersale.dao;

import lombok.RequiredArgsConstructor;

import org.oomall.labaftersale.mapper.AftersaleRepository;
import org.oomall.labaftersale.mapper.po.AftersalePo;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AftersaleDao {

    private final AftersaleRepository aftersaleRepository;

    public AftersalePo findByIdAndShopId(Long id, Long shopId) {
        return aftersaleRepository.findByIdAndShopId(id, shopId);
    }

    public void save(AftersalePo po) {
        aftersaleRepository.save(po);
    }

    // 新增：校验订单商品是否已提交售后
    public boolean existsByOrderItemId(Long orderItemId) {
        return aftersaleRepository.existsByOrderItemId(orderItemId);
    }
}
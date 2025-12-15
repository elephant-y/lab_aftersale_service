package org.oomall.labaftersale.dao;

import lombok.RequiredArgsConstructor;
import org.oomall.labaftersale.mapper.AftersaleMapper;
import org.oomall.labaftersale.mapper.po.AftersalePo;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AftersaleDao {

    private final AftersaleMapper aftersaleMapper;

    public AftersalePo findByIdAndShopId(Long id, Long shopId) {
        return aftersaleMapper.findByIdAndShopId(id, shopId);
    }

    public void save(AftersalePo po) {
        aftersaleMapper.save(po);
    }
}
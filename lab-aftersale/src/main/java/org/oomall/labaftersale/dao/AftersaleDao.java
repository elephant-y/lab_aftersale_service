package org.oomall.labaftersale.dao;

import lombok.RequiredArgsConstructor;

import org.oomall.labaftersale.mapper.AftersaleRepository;
import org.oomall.labaftersale.mapper.po.AftersalePo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 独立事务插入：并发下唯一索引冲突只回滚这个内层事务，
     * 不会把外层事务标记为 rollback-only（否则外层吞掉异常后提交会抛 UnexpectedRollbackException）
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveNew(AftersalePo po) {
        aftersaleRepository.save(po);
    }

    // 新增：校验订单商品是否已提交售后
    public boolean existsByOrderItemId(Long orderItemId) {
        return aftersaleRepository.existsByOrderItemId(orderItemId);
    }
}
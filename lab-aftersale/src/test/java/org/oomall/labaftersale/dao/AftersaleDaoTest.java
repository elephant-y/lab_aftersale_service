package org.oomall.labaftersale.dao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oomall.labaftersale.mapper.AftersaleRepository;
import org.oomall.labaftersale.mapper.po.AftersalePo;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
@ExtendWith(MockitoExtension.class)
class AftersaleDaoTest {

    @Mock
    private AftersaleRepository aftersaleRepository;

    @InjectMocks
    private AftersaleDao aftersaleDao;

    /**
     * 测试：根据 id 和 shopId 查询售后单
     */
    @Test
    void findByIdAndShopId_success() {
        Long aftersaleId = 1L;
        Long shopId = 2L;

        AftersalePo po = new AftersalePo();
        po.setId(aftersaleId);
        po.setShopId(shopId);

        when(aftersaleRepository.findByIdAndShopId(aftersaleId, shopId))
                .thenReturn(po);

        AftersalePo result = aftersaleDao.findByIdAndShopId(aftersaleId, shopId);

        assertNotNull(result);
        assertEquals(aftersaleId, result.getId());
        assertEquals(shopId, result.getShopId());

        verify(aftersaleRepository)
                .findByIdAndShopId(aftersaleId, shopId);
    }

    /**
     * 测试：保存售后单
     */
    @Test
    void save_success() {
        AftersalePo po = new AftersalePo();
        po.setId(1L);

        aftersaleDao.save(po);

        verify(aftersaleRepository).save(po);
    }

    /**
     * 测试：订单商品是否已存在售后（存在）
     */
    @Test
    void existsByOrderItemId_true() {
        Long orderItemId = 100L;

        when(aftersaleRepository.existsByOrderItemId(orderItemId))
                .thenReturn(true);

        boolean result = aftersaleDao.existsByOrderItemId(orderItemId);

        assertTrue(result);
        verify(aftersaleRepository)
                .existsByOrderItemId(orderItemId);
    }

    /**
     * 测试：订单商品是否已存在售后（不存在）
     */
    @Test
    void existsByOrderItemId_false() {
        Long orderItemId = 101L;

        when(aftersaleRepository.existsByOrderItemId(orderItemId))
                .thenReturn(false);

        boolean result = aftersaleDao.existsByOrderItemId(orderItemId);

        assertFalse(result);
        verify(aftersaleRepository)
                .existsByOrderItemId(orderItemId);
    }
}

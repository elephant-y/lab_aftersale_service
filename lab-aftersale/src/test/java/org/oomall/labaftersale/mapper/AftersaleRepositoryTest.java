package org.oomall.labaftersale.mapper;

import org.junit.jupiter.api.Test;
import org.oomall.labaftersale.mapper.po.AftersalePo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class AftersaleRepositoryTest {

    @Autowired
    private AftersaleRepository aftersaleRepository;

    /**
     * 测试：findByIdAndShopId 能正确查询
     */
    @Test
    void findByIdAndShopId_success() {
        AftersalePo po = new AftersalePo();
        po.setShopId(1L);
        po.setOrderItemId(100L);


        AftersalePo saved = aftersaleRepository.save(po);

        AftersalePo result =
                aftersaleRepository.findByIdAndShopId(saved.getId(), 1L);

        assertNotNull(result);
    }

    /**
     * 测试：existsByOrderItemId = true
     */
    @Test
    void existsByOrderItemId_true() {
        AftersalePo po = new AftersalePo();
        po.setShopId(1L);
        po.setOrderItemId(200L);




        aftersaleRepository.save(po);

        boolean exists =
                aftersaleRepository.existsByOrderItemId(200L);

        assertTrue(exists);
    }

    /**
     * 测试：existsByOrderItemId = false
     */
    @Test
    void existsByOrderItemId_false() {
        boolean exists =
                aftersaleRepository.existsByOrderItemId(999L);

        assertFalse(exists);
    }
}

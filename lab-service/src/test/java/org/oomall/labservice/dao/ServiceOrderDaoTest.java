package org.oomall.labservice.dao;





import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oomall.labservice.mapper.Po.ServiceOrderPo;
import org.oomall.labservice.mapper.ServiceOrderRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOrderDaoTest {

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @InjectMocks
    private ServiceOrderDao serviceOrderDao;

    private static final Long ORDER_ID = 300L;
    private static final Long SHOP_ID = 100L;
    private static final Long MAINTAINER_ID = 400L;
    private static final Long AFTERSALE_ID = 200L;

    // 构建一个模拟的 ServiceOrderPo
    private ServiceOrderPo buildOrderPo() {
        ServiceOrderPo po = new ServiceOrderPo();
        po.setId(ORDER_ID);
        po.setShopId(SHOP_ID);
        po.setAftersaleId(AFTERSALE_ID);
        po.setServiceType(1);
        po.setStatus(0);
        po.setConsigneeRegionId(500);
        po.setConsigneeName("张三");
        po.setConsigneeMobile("13800000000");
        po.setConsigneeAddress("北京市");
        po.setMaintainerId(MAINTAINER_ID);
        return po;
    }

    @Test
    @DisplayName("findByIdAndShopId - 找到记录时返回 Po")
    void findByIdAndShopId_shouldReturnPo_whenFound() {
        ServiceOrderPo po = buildOrderPo();
        when(serviceOrderRepository.findByIdAndShopId(eq(ORDER_ID), eq(SHOP_ID)))
                .thenReturn(po);

        ServiceOrderPo result = serviceOrderDao.findByIdAndShopId(ORDER_ID, SHOP_ID);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(po);
        verify(serviceOrderRepository, times(1)).findByIdAndShopId(ORDER_ID, SHOP_ID);
    }

    @Test
    @DisplayName("findByIdAndShopId - 未找到记录时返回 null")
    void findByIdAndShopId_shouldReturnNull_whenNotFound() {
        when(serviceOrderRepository.findByIdAndShopId(eq(ORDER_ID), eq(SHOP_ID)))
                .thenReturn(null);

        ServiceOrderPo result = serviceOrderDao.findByIdAndShopId(ORDER_ID, SHOP_ID);

        assertThat(result).isNull();
        verify(serviceOrderRepository, times(1)).findByIdAndShopId(ORDER_ID, SHOP_ID);
    }

    @Test
    @DisplayName("findByIdAndMaintainerId - 找到记录时返回 Po")
    void findByIdAndMaintainerId_shouldReturnPo_whenFound() {
        ServiceOrderPo po = buildOrderPo();
        when(serviceOrderRepository.findByIdAndMaintainerId(eq(ORDER_ID), eq(MAINTAINER_ID)))
                .thenReturn(po);

        ServiceOrderPo result = serviceOrderDao.findByIdAndMaintainerId(ORDER_ID, MAINTAINER_ID);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(po);
        verify(serviceOrderRepository, times(1)).findByIdAndMaintainerId(ORDER_ID, MAINTAINER_ID);
    }

    @Test
    @DisplayName("findByIdAndMaintainerId - 未找到记录时返回 null")
    void findByIdAndMaintainerId_shouldReturnNull_whenNotFound() {
        when(serviceOrderRepository.findByIdAndMaintainerId(eq(ORDER_ID), eq(MAINTAINER_ID)))
                .thenReturn(null);

        ServiceOrderPo result = serviceOrderDao.findByIdAndMaintainerId(ORDER_ID, MAINTAINER_ID);

        assertThat(result).isNull();
        verify(serviceOrderRepository, times(1)).findByIdAndMaintainerId(ORDER_ID, MAINTAINER_ID);
    }

    @Test
    @DisplayName("existsByAftersaleId - 存在记录时返回 true")
    void existsByAftersaleId_shouldReturnTrue_whenCountGreaterThanZero() {
        when(serviceOrderRepository.countByAftersaleId(eq(AFTERSALE_ID))).thenReturn(1);

        boolean result = serviceOrderDao.existsByAftersaleId(AFTERSALE_ID);

        assertThat(result).isTrue();
        verify(serviceOrderRepository, times(1)).countByAftersaleId(AFTERSALE_ID);
    }

    @Test
    @DisplayName("existsByAftersaleId - 不存在记录时返回 false")
    void existsByAftersaleId_shouldReturnFalse_whenCountZero() {
        when(serviceOrderRepository.countByAftersaleId(eq(AFTERSALE_ID))).thenReturn(0);

        boolean result = serviceOrderDao.existsByAftersaleId(AFTERSALE_ID);

        assertThat(result).isFalse();
        verify(serviceOrderRepository, times(1)).countByAftersaleId(AFTERSALE_ID);
    }

    @Test
    @DisplayName("insert - 正常保存并返回 ID")
    void insert_shouldSaveAndReturnId() {
        ServiceOrderPo po = buildOrderPo();
        // 模拟 save 返回的对象（ID 已设置）
        ServiceOrderPo savedPo = buildOrderPo();
        when(serviceOrderRepository.save(eq(po))).thenReturn(savedPo);

        int returnedId = serviceOrderDao.insert(po);

        assertThat(returnedId).isEqualTo(ORDER_ID.intValue());
        verify(serviceOrderRepository, times(1)).save(po);

    }
}
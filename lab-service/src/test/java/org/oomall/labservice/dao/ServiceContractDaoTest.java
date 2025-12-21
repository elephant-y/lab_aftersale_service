package org.oomall.labservice.dao;



import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oomall.labservice.mapper.Po.ServiceContractPo;
import org.oomall.labservice.mapper.ServiceContractRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceContractDaoTest {

    @Mock
    private ServiceContractRepository serviceContractRepository;

    @InjectMocks
    private ServiceContractDao serviceContractDao;

    private static final Long SHOP_ID = 100L;
    private static final Long REGION_ID = 200L;
    private static final Integer SERVICE_TYPE = 1;
    private static final Long CONTRACT_ID = 300L;

    // 构建一个模拟的 ServiceContractPo 对象
    private ServiceContractPo buildContractPo() {
        ServiceContractPo po = new ServiceContractPo();
        po.setId(CONTRACT_ID);
        po.setShopId(SHOP_ID);
        po.setRegionId(REGION_ID);
        po.setServiceType(SERVICE_TYPE);
        po.setMaintainerId(400L);
        // 根据你的 PO 类实际字段补充其他属性（如 status、gmtCreate 等）
        return po;
    }

    @Test
    @DisplayName("查询可分配服务合同 - 找到匹配记录时返回合同对象")
    void findToAssign_shouldReturnContractPo_whenMatchFound() {
        // Given: Repository 返回一个有效的合同对象
        ServiceContractPo po = buildContractPo();
        when(serviceContractRepository.findByShopIdAndRegionIdAndServiceType(
                eq(SHOP_ID), eq(REGION_ID), eq(SERVICE_TYPE)))
                .thenReturn(po);

        // When
        ServiceContractPo result = serviceContractDao.findToAssign(SHOP_ID, REGION_ID, SERVICE_TYPE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(po);
        assertThat(result.getId()).isEqualTo(CONTRACT_ID);

        // 验证 Repository 方法被正确调用一次
        verify(serviceContractRepository, times(1))
                .findByShopIdAndRegionIdAndServiceType(SHOP_ID, REGION_ID, SERVICE_TYPE);
    }

    @Test
    @DisplayName("查询可分配服务合同 - 未找到匹配记录时返回 null")
    void findToAssign_shouldReturnNull_whenNoMatchFound() {
        // Given: Repository 返回 null（表示没有符合条件的记录）
        when(serviceContractRepository.findByShopIdAndRegionIdAndServiceType(
                eq(SHOP_ID), eq(REGION_ID), eq(SERVICE_TYPE)))
                .thenReturn(null);

        // When
        ServiceContractPo result = serviceContractDao.findToAssign(SHOP_ID, REGION_ID, SERVICE_TYPE);

        // Then
        assertThat(result).isNull();

        verify(serviceContractRepository, times(1))
                .findByShopIdAndRegionIdAndServiceType(SHOP_ID, REGION_ID, SERVICE_TYPE);
    }
}

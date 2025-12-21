package org.oomall.labservice.mapper;



import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oomall.labservice.mapper.Po.ServiceContractPo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest  // 只加载 JPA 组件，使用 H2 内存数据库
class ServiceContractRepositoryTest {

    @Autowired
    private ServiceContractRepository serviceContractRepository;

    private static final Long SHOP_ID = 100L;
    private static final Long REGION_ID = 200L;
    private static final Integer SERVICE_TYPE = 1;
    private static final Long MAINTAINER_ID = 400L;
    private static final Long CONTRACT_ID_1 = 300L;
    private static final Long CONTRACT_ID_2 = 301L;

    // 构建一个模拟的 ServiceContractPo 对象
    private ServiceContractPo buildContractPo(Long id, Long shopId, Long regionId, Integer serviceType) {
        ServiceContractPo po = new ServiceContractPo();
        po.setId(id);
        po.setShopId(shopId);
        po.setRegionId(regionId);
        po.setServiceType(serviceType);
        po.setMaintainerId(MAINTAINER_ID);
        // 根据你的 ServiceContractPo 实际字段补充其他属性（如 status、gmtCreate 等）
        return po;
    }

    @Test
    @DisplayName("findByShopIdAndRegionIdAndServiceType - 匹配时返回合同对象")
    void findByShopIdAndRegionIdAndServiceType_shouldReturnPo_whenMatch() {
        // Given: 保存一条完全匹配的记录（不要写死 id）
        ServiceContractPo po = buildContractPo(null, SHOP_ID, REGION_ID, SERVICE_TYPE);
        serviceContractRepository.save(po);

        // When
        ServiceContractPo result = serviceContractRepository
                .findByShopIdAndRegionIdAndServiceType(SHOP_ID, REGION_ID, SERVICE_TYPE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();                 // 核心修改点
        assertThat(result.getShopId()).isEqualTo(SHOP_ID);
        assertThat(result.getRegionId()).isEqualTo(REGION_ID);
        assertThat(result.getServiceType()).isEqualTo(SERVICE_TYPE);
    }

    @Test
    @DisplayName("findByShopIdAndRegionIdAndServiceType - 不匹配时返回 null")
    void findByShopIdAndRegionIdAndServiceType_shouldReturnNull_whenNoMatch() {
        // Given: 保存一条记录，但某些条件不匹配
        ServiceContractPo po = buildContractPo(CONTRACT_ID_1, SHOP_ID, REGION_ID, 999); // serviceType 不匹配
        serviceContractRepository.save(po);

        // 再保存一条 shopId 不匹配的
        ServiceContractPo po2 = buildContractPo(CONTRACT_ID_2, 999L, REGION_ID, SERVICE_TYPE);
        serviceContractRepository.save(po2);

        // When: 查询完全匹配的条件（无记录）
        ServiceContractPo result = serviceContractRepository
                .findByShopIdAndRegionIdAndServiceType(SHOP_ID, REGION_ID, SERVICE_TYPE);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("findByShopIdAndRegionIdAndServiceType - 数据库为空时返回 null")
    void findByShopIdAndRegionIdAndServiceType_shouldReturnNull_whenEmpty() {
        // When: 直接查询（无任何记录）
        ServiceContractPo result = serviceContractRepository
                .findByShopIdAndRegionIdAndServiceType(SHOP_ID, REGION_ID, SERVICE_TYPE);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("save - 正常保存合同并自动生成 ID（顺便覆盖 JpaRepository 的 save）")
    void save_shouldPersistAndAssignId() {
        // Given: 新实体，ID 为 null
        ServiceContractPo po = buildContractPo(null, SHOP_ID, REGION_ID, SERVICE_TYPE);

        // When
        ServiceContractPo saved = serviceContractRepository.save(po);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isPositive();

        // 验证可以再次查询到
        ServiceContractPo queried = serviceContractRepository
                .findByShopIdAndRegionIdAndServiceType(SHOP_ID, REGION_ID, SERVICE_TYPE);
        assertThat(queried).isNotNull();
        assertThat(queried.getId()).isEqualTo(saved.getId());
    }
}

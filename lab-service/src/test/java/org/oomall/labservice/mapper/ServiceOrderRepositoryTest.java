package org.oomall.labservice.mapper;



import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oomall.labservice.mapper.Po.ServiceOrderPo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest  // 只加载 JPA 组件，使用 H2 内存数据库
class ServiceOrderRepositoryTest {

    @Autowired
    private ServiceOrderRepository serviceOrderRepository;

    private static final Long SHOP_ID = 100L;
    private static final Long MAINTAINER_ID = 400L;
    private static final Long AFTERSALE_ID = 200L;
    private static final Long ORDER_ID_1 = 300L;
    private static final Long ORDER_ID_2 = 301L;
    private static final Long ORDER_ID_3 = 302L;

    // 构建一个基础 ServiceOrderPo
    private ServiceOrderPo buildOrderPo(Long id, Long shopId, Long maintainerId, Long aftersaleId) {
        ServiceOrderPo po = new ServiceOrderPo();
        po.setId(id);
        po.setShopId(shopId);
        po.setAftersaleId(aftersaleId);
        po.setServiceType(1);
        po.setStatus(0);
        po.setConsigneeRegionId(500);
        po.setConsigneeName("张三");
        po.setConsigneeMobile("13800000000");
        po.setConsigneeAddress("北京市");
        if (maintainerId != null) {
            po.setMaintainerId(maintainerId);
        }
        return po;
    }

    @Test
    @DisplayName("findByIdAndShopId - 匹配时返回 Po")
    void findByIdAndShopId_shouldReturnPo_whenMatch() {
        // Given：id 一定要传 null
        ServiceOrderPo po = buildOrderPo(null, SHOP_ID, null, AFTERSALE_ID);
        ServiceOrderPo saved = serviceOrderRepository.save(po);

        Long generatedId = saved.getId();
        assertThat(generatedId).isNotNull();

        // When：用真实生成的 id 查
        ServiceOrderPo result =
                serviceOrderRepository.findByIdAndShopId(generatedId, SHOP_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(generatedId);
        assertThat(result.getShopId()).isEqualTo(SHOP_ID);
    }

    @Test
    @DisplayName("findByIdAndShopId - 不匹配时返回 null")
    void findByIdAndShopId_shouldReturnNull_whenNoMatch() {
        ServiceOrderPo po = buildOrderPo(ORDER_ID_1, SHOP_ID, null, AFTERSALE_ID);
        serviceOrderRepository.save(po);

        // shopId 不匹配
        ServiceOrderPo result = serviceOrderRepository.findByIdAndShopId(ORDER_ID_1, 999L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("findByIdAndMaintainerId - 匹配时返回 Po")
    void findByIdAndMaintainerId_shouldReturnPo_whenMatch() {
        // Given：不要写死 id
        ServiceOrderPo po = buildOrderPo(null, SHOP_ID, MAINTAINER_ID, AFTERSALE_ID);
        ServiceOrderPo saved = serviceOrderRepository.save(po);

        Long generatedId = saved.getId();
        assertThat(generatedId).isNotNull();

        // When：用数据库生成的 id 查询
        ServiceOrderPo result =
                serviceOrderRepository.findByIdAndMaintainerId(generatedId, MAINTAINER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(generatedId);
        assertThat(result.getMaintainerId()).isEqualTo(MAINTAINER_ID);
    }

    @Test
    @DisplayName("findByIdAndMaintainerId - 不匹配或 maintainerId 为 null 时返回 null")
    void findByIdAndMaintainerId_shouldReturnNull_whenNoMatchOrNullMaintainer() {
        // maintainerId 为 null 的记录
        ServiceOrderPo po1 = buildOrderPo(ORDER_ID_1, SHOP_ID, null, AFTERSALE_ID);
        serviceOrderRepository.save(po1);

        // maintainerId 不匹配的记录
        ServiceOrderPo po2 = buildOrderPo(ORDER_ID_2, SHOP_ID, 999L, AFTERSALE_ID);
        serviceOrderRepository.save(po2);

        ServiceOrderPo result1 = serviceOrderRepository.findByIdAndMaintainerId(ORDER_ID_1, MAINTAINER_ID);
        ServiceOrderPo result2 = serviceOrderRepository.findByIdAndMaintainerId(ORDER_ID_2, MAINTAINER_ID);

        assertThat(result1).isNull();
        assertThat(result2).isNull();
    }

    @Test
    @DisplayName("countByAftersaleId - 统计正确数量")
    void countByAftersaleId_shouldReturnCorrectCount() {
        // Given: 保存三条记录，其中两条 aftersaleId 相同
        serviceOrderRepository.save(buildOrderPo(ORDER_ID_1, SHOP_ID, null, AFTERSALE_ID));
        serviceOrderRepository.save(buildOrderPo(ORDER_ID_2, SHOP_ID + 1, null, AFTERSALE_ID)); // 相同 aftersaleId
        serviceOrderRepository.save(buildOrderPo(ORDER_ID_3, SHOP_ID, null, 999L)); // 不同 aftersaleId

        // When
        int count = serviceOrderRepository.countByAftersaleId(AFTERSALE_ID);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("countByAftersaleId - 无记录时返回 0")
    void countByAftersaleId_shouldReturnZero_whenNoRecords() {
        int count = serviceOrderRepository.countByAftersaleId(999L);

        assertThat(count).isZero();
    }

    @Test
    @DisplayName("save - 正常保存并自动生成 ID（顺便覆盖 JpaRepository 的 save）")
    void save_shouldPersistAndAssignId() {
        ServiceOrderPo po = buildOrderPo(null, SHOP_ID, null, AFTERSALE_ID);

        ServiceOrderPo saved = serviceOrderRepository.save(po);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isPositive();
    }
}

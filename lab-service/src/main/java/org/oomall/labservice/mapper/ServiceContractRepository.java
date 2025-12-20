package org.oomall.labservice.mapper;

import org.oomall.labservice.mapper.Po.ServiceContractPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceContractRepository extends JpaRepository<ServiceContractPo, Long> {
    // 关键修改：返回类型从 List<ServiceContractPo> → ServiceContractPo
    // JPA规则：即使匹配多条，也会返回第一条；无匹配返回null（你保证唯一，无需担心多条）
    ServiceContractPo findByShopIdAndRegionIdAndServiceType(
            Long shopId, Long regionId, Integer serviceType);
}
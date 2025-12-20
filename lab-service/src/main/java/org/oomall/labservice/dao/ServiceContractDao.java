package org.oomall.labservice.dao;

import lombok.RequiredArgsConstructor;
import org.oomall.labservice.mapper.ServiceContractRepository;
import org.oomall.labservice.mapper.Po.ServiceContractPo;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ServiceContractDao {
    private final ServiceContractRepository serviceContractRepository;

    // 关键修改：返回类型从 List<ServiceContractPo> → ServiceContractPo
    public ServiceContractPo findToAssign(Long shopId, Long regionId, Integer serviceType) {
        return serviceContractRepository.findByShopIdAndRegionIdAndServiceType(shopId, regionId, serviceType);
    }
}
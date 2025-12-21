package org.oomall.labservice.mapper;

import org.oomall.labservice.mapper.Po.ServiceContractPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceContractRepository extends JpaRepository<ServiceContractPo, Long> {


    ServiceContractPo findByShopIdAndRegionIdAndServiceType(
            Long shopId, Long regionId, Integer serviceType);
}
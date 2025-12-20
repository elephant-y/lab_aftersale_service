package org.oomall.labservice.dao;

import lombok.RequiredArgsConstructor;
import org.oomall.labservice.mapper.Po.ServiceOrderPo;
import org.oomall.labservice.mapper.ServiceOrderRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ServiceOrderDao {

    private final ServiceOrderRepository serviceOrderRepository;

    public ServiceOrderPo findByIdAndShopId(Long id, Long shopId) {
        return serviceOrderRepository.findByIdAndShopId(id, shopId);
    }

    public boolean existsByAftersaleId(Long aftersaleId) {
        return serviceOrderRepository.countByAftersaleId(aftersaleId) > 0;
    }

    public int insert(ServiceOrderPo serviceOrderPo) {
        return serviceOrderRepository.save(serviceOrderPo).getId().intValue();
    }
}

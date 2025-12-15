package org.oomall.labservice.dao;

import lombok.RequiredArgsConstructor;
import org.oomall.labservice.mapper.Po.ServiceOrderPo;
import org.oomall.labservice.mapper.ServiceOrderMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ServiceOrderDao {
    private final ServiceOrderMapper serviceOrderMapper;
    public boolean existsByAftersaleId(Long aftersaleId) {
        return serviceOrderMapper.countByAftersaleId(aftersaleId) > 0;
    }

    public int insert(ServiceOrderPo serviceOrderPo) {
        return serviceOrderMapper.insert(serviceOrderPo);
    }
}

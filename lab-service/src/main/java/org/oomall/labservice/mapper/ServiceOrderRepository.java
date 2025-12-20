package org.oomall.labservice.mapper;


import org.oomall.labservice.mapper.Po.ServiceOrderPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrderPo, Long> {

    ServiceOrderPo findByIdAndShopId(Long id, Long shopId);

    ServiceOrderPo findByIdAndMaintainerId(Long id, Long maintainerId);

    // 统计 aftersaleId 出现次数
    int countByAftersaleId(Long aftersaleId);
}
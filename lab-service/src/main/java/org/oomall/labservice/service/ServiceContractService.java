package org.oomall.labservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oomall.labservice.dao.ServiceContractDao;
import org.oomall.labservice.mapper.Po.ServiceContractPo;
import org.oomall.labservice.model.ReturnNo;
import org.oomall.labservice.model.ReturnObject;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceContractService {
    private final ServiceContractDao serviceContractDao;

    // 关键修改：处理单条结果，新增空值校验（无匹配返回资源不存在）
    public ReturnObject findToAssign(Long shopId, Long regionId, Integer serviceType) {
        try {
            ServiceContractPo contract = serviceContractDao.findToAssign(shopId, regionId, serviceType);
            if (contract == null) {
                log.error("【服务模块】未查询到可分配的服务合同，商户ID={}，区域ID={}，服务类型={}",
                        shopId, regionId, serviceType);
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST); // 无匹配返回资源不存在
            }
            log.info("【服务模块】查询可分配服务合同成功，合同ID={}，商户ID={}", contract.getId(), shopId);
            return new ReturnObject(ReturnNo.OK, contract);
        } catch (Exception e) {
            log.error("【服务模块】查询可分配服务合同异常，商户ID={}，区域ID={}，服务类型={}",
                    shopId, regionId, serviceType, e);
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
    }
}
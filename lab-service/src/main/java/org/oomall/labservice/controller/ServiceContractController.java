package org.oomall.labservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oomall.labservice.model.ReturnObject;
import org.oomall.labservice.service.ServiceContractService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/internal/shops/{shopId}/service-contracts/assign")
@RequiredArgsConstructor
public class ServiceContractController {
    private final ServiceContractService serviceContractService;

    // 返回单条合同数据（而非数组）
    @GetMapping("/{regionId}/{serviceType}")
    public ReturnObject findToAssign(
            @PathVariable Long shopId,
            @PathVariable Long regionId,
            @PathVariable Integer serviceType) {
        log.info("【服务模块】接收查询可分配服务合同请求，商户ID={}，区域ID={}，服务类型={}",
                shopId, regionId, serviceType);
        return serviceContractService.findToAssign(shopId, regionId, serviceType);
    }
}
package org.oomall.labservice.controller;

import lombok.RequiredArgsConstructor;
import org.oomall.labservice.model.ReturnNo;
import org.oomall.labservice.model.ReturnObject;
import org.oomall.labservice.service.ServiceOrderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/internal/shops/{shopId}/aftersales")
@RequiredArgsConstructor
public class ServiceOrderController {
    private final ServiceOrderService serviceOrderService;
    @PostMapping("/{id}/serviceorders")
    public ReturnObject createServiceOrder(@PathVariable("shopId") Long shopId,@PathVariable("id") Long id) {
        log.info("【服务模块】创建服务单请求，shopId={}, aftersaleId={}", shopId, id);
        return serviceOrderService.createServiceOrder(shopId, id);
    }
}

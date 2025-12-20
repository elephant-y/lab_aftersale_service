package org.oomall.labservice.controller;

import lombok.RequiredArgsConstructor;
import org.oomall.labservice.controller.dto.ServiceOrderDto;
import org.oomall.labservice.mapper.Po.ServiceOrderPo;
import org.oomall.labservice.model.ReturnNo;
import org.oomall.labservice.model.ReturnObject;
import org.oomall.labservice.service.ServiceOrderService;
import org.oomall.labservice.service.vo.ServiceOrderVo;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/internal/shops/{shopId}")
@RequiredArgsConstructor
public class ServiceOrderController {
    private final ServiceOrderService serviceOrderService;

    @PostMapping("/aftersales/{id}/serviceorders")
    public ReturnObject createServiceOrder(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long id,
            @RequestBody ServiceOrderDto dto)
    {
        log.info("【服务模块】创建服务单请求，shopId={}, aftersaleId={}, serviceType={}",
                shopId, id, dto.getServiceType());
        return serviceOrderService.createServiceOrder(shopId, id, dto);
    }

    @GetMapping("/serviceorders/{orderId}")
    public ReturnObject getServiceOrder(
            @PathVariable("shopId") Long shopId,
            @PathVariable("orderId") Long orderId) {
        log.info("【服务模块】查询服务单请求，shopId={}, serviceOrderId={}", shopId, orderId);
        // 调用服务层查询方法
        ServiceOrderPo serviceOrder = serviceOrderService.getServiceOrder(shopId, orderId);
        if (serviceOrder == null) {
            log.error("【服务模块】服务单不存在，shopId={}, serviceOrderId={}", shopId, orderId);
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST); // 对应errno=504
        }
        // 转换为VO返回（与创建接口保持一致的返回格式）
        ServiceOrderVo vo = new ServiceOrderVo();
        vo.setId(serviceOrder.getId());
        vo.setServiceType(serviceOrder.getServiceType());
        return new ReturnObject(vo);
    }
}
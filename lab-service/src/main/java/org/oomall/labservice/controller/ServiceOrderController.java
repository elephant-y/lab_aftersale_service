package org.oomall.labservice.controller;

import lombok.RequiredArgsConstructor;
import org.oomall.labservice.controller.dto.ServiceOrderDto;
import org.oomall.labservice.mapper.Po.ServiceOrderPo;
import org.oomall.labservice.model.ReturnNo;
import org.oomall.labservice.model.ReturnObject;
import org.oomall.labservice.model.ServiceOrderStatus;
import org.oomall.labservice.service.ServiceOrderService;
import org.oomall.labservice.service.vo.ServiceOrderDetailVo;
import org.oomall.labservice.service.vo.ServiceOrderVo;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping()
@RequiredArgsConstructor
public class ServiceOrderController {
    private final ServiceOrderService serviceOrderService;

    @PostMapping("/internal/shops/{shopId}/aftersales/{id}/serviceorders")
    public ReturnObject createServiceOrder(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long id,
            @RequestBody ServiceOrderDto dto)
    {
        log.info("【服务模块】创建服务单请求，shopId={}, aftersaleId={}, serviceType={}",
                shopId, id, dto.getServiceType());
        return serviceOrderService.createServiceOrder(shopId, id, dto);
    }

    @GetMapping("/internal/shops/{shopId}/serviceorders/{orderId}")
    public ReturnObject findServiceOrderByShopId(
            @PathVariable("shopId") Long shopId,
            @PathVariable("orderId") Long orderId) {
        log.info("【服务模块】查询服务单请求，shopId={}, serviceOrderId={}", shopId, orderId);
        // 调用服务层查询方法
        ServiceOrderPo serviceOrder = serviceOrderService.findServiceOrderByShopId(shopId, orderId);
        if (serviceOrder == null) {
            log.error("【服务模块】服务单不存在，shopId={}, serviceOrderId={}", shopId, orderId);
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST); // 对应errno=504
        }
        // 转换为VO返回（与创建接口保持一致的返回格式）
        ServiceOrderDetailVo vo = new ServiceOrderDetailVo();

        vo.setId(serviceOrder.getId());
        vo.setShopId(serviceOrder.getShopId());
        vo.setAftersaleId(serviceOrder.getAftersaleId());
        vo.setServiceType(serviceOrder.getServiceType());
        vo.setStatus(ServiceOrderStatus.values()[serviceOrder.getStatus()]); // 转换状态为枚举
        vo.setGmtCreate(serviceOrder.getGmtCreate());
        vo.setGmtModified(serviceOrder.getGmtModified());

        // 封装收货信息
        ServiceOrderDetailVo.ConsigneeVo consigneeVo = new ServiceOrderDetailVo.ConsigneeVo();
        consigneeVo.setConsigneeName(serviceOrder.getConsigneeName());
        consigneeVo.setConsigneeMobile(serviceOrder.getConsigneeMobile());
        consigneeVo.setConsigneeRegionId(serviceOrder.getConsigneeRegionId());
        consigneeVo.setConsigneeAddress(serviceOrder.getConsigneeAddress());
        vo.setConsignee(consigneeVo);

        // 封装维修人员信息
        ServiceOrderDetailVo.MaintainerVo maintainerVo = new ServiceOrderDetailVo.MaintainerVo();
        maintainerVo.setId(serviceOrder.getMaintainerId());
        maintainerVo.setMaintainerName(serviceOrder.getMaintainerName());
        maintainerVo.setMaintainerMobile(serviceOrder.getMaintainerMobile());
        maintainerVo.setMaintainerAddress(serviceOrder.getMaintainerAddress());
        vo.setMaintainer(maintainerVo);

        return new ReturnObject(vo);
    }

    @GetMapping("/internal/serviceorders/{orderId}/maintainers/{maintainerId}")
    public ReturnObject getServiceOrderByMaintainer(
            @PathVariable("orderId") Long orderId,
            @PathVariable("maintainerId") Long maintainerId) {
        log.info("【服务模块】按维修人员查询服务单请求，serviceOrderId={}, maintainerId={}", orderId, maintainerId);
        ServiceOrderPo serviceOrder = serviceOrderService.findServiceOrderByMaintainerId(maintainerId, orderId);
        if (serviceOrder == null) {
            log.error("【服务模块】服务单不存在，serviceOrderId={}, maintainerId={}", orderId, maintainerId);
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
        // 转换为VO返回（复用已有的ServiceOrderDetailVo）
        ServiceOrderDetailVo vo = new ServiceOrderDetailVo();

        vo.setId(serviceOrder.getId());
        vo.setShopId(serviceOrder.getShopId());
        vo.setAftersaleId(serviceOrder.getAftersaleId());
        vo.setServiceType(serviceOrder.getServiceType());
        vo.setStatus(ServiceOrderStatus.values()[serviceOrder.getStatus()]); // 转换状态为枚举
        vo.setGmtCreate(serviceOrder.getGmtCreate());
        vo.setGmtModified(serviceOrder.getGmtModified());

        // 封装收货信息
        ServiceOrderDetailVo.ConsigneeVo consigneeVo = new ServiceOrderDetailVo.ConsigneeVo();
        consigneeVo.setConsigneeName(serviceOrder.getConsigneeName());
        consigneeVo.setConsigneeMobile(serviceOrder.getConsigneeMobile());
        consigneeVo.setConsigneeRegionId(serviceOrder.getConsigneeRegionId());
        consigneeVo.setConsigneeAddress(serviceOrder.getConsigneeAddress());
        vo.setConsignee(consigneeVo);

        // 封装维修人员信息
        ServiceOrderDetailVo.MaintainerVo maintainerVo = new ServiceOrderDetailVo.MaintainerVo();
        maintainerVo.setId(serviceOrder.getMaintainerId());
        maintainerVo.setMaintainerName(serviceOrder.getMaintainerName());
        maintainerVo.setMaintainerMobile(serviceOrder.getMaintainerMobile());
        maintainerVo.setMaintainerAddress(serviceOrder.getMaintainerAddress());
        vo.setMaintainer(maintainerVo);

        return new ReturnObject(vo);
    }

    @PostMapping("/shops/{shopId}/serviceorders/{orderId}/assign")
    public ReturnObject assignServiceOrder(
            @PathVariable("shopId") Long shopId,
            @PathVariable("orderId") Long orderId) {
        log.info("【服务模块】分配服务单请求，shopId={}, serviceOrderId={}", shopId, orderId);
        return serviceOrderService.assignServiceOrder(shopId, orderId);
    }

    /**
     * 维修人员拒绝服务单
     * @param maintainerId 维修人员ID
     * @param orderId 服务单ID
     * @return 处理结果
     */
    @PostMapping("/internal/maintainers/{maintainerId}/serviceorders/{orderId}/reject")
    public ReturnObject refuseServiceOrder(
            @PathVariable("maintainerId") Long maintainerId,
            @PathVariable("orderId") Long orderId) {
        log.info("【服务模块】维修人员拒绝服务单请求，maintainerId={}, serviceOrderId={}", maintainerId, orderId);
        return serviceOrderService.refuseServiceOrder(maintainerId, orderId);
    }

    /**
     * 维修人员完成服务单
     * @param maintainerId 维修人员ID
     * @param orderId 服务单ID
     * @return 处理结果
     */
    @PostMapping("/internal/maintainers/{maintainerId}/serviceorders/{orderId}/finish")
    public ReturnObject finishServiceOrder(
            @PathVariable("maintainerId") Long maintainerId,
            @PathVariable("orderId") Long orderId) {
        log.info("【服务模块】维修人员完成服务单请求，maintainerId={}, serviceOrderId={}", maintainerId, orderId);
        return serviceOrderService.finishServiceOrder(maintainerId, orderId);
    }
}
package org.oomall.labservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.oomall.labservice.dao.MaintainerDao;
import org.oomall.labservice.mapper.Po.MaintainerPo;
import org.oomall.labservice.mapper.Po.ServiceContractPo;
import org.oomall.labservice.model.*;
import org.oomall.labservice.controller.dto.ServiceOrderDto;
import org.oomall.labservice.dao.ServiceOrderDao;
import org.oomall.labservice.feign.AftersaleFeign;

import org.oomall.labservice.feign.po.AftersalePo;
import org.oomall.labservice.mapper.Po.ServiceOrderPo;
import org.oomall.labservice.service.servicetype.ServiceTypeHandler;
import org.oomall.labservice.service.servicetype.ServiceTypeHandlerFactory;
import org.oomall.labservice.service.vo.ServiceOrderVo;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Slf4j
@RequiredArgsConstructor
@Service
public class ServiceOrderService {
    private final ServiceOrderDao serviceOrderDao;
    private final AftersaleFeign aftersaleFeign;
    private final ServiceContractService serviceContractService;
    private final MaintainerDao maintainerDao;
    private final ServiceTypeHandlerFactory handlerFactory;



    public ServiceOrderPo findServiceOrderByShopId(Long shopId, Long id) {
        ServiceOrderPo po = serviceOrderDao.findByIdAndShopId(id, shopId);
        return po;
    }

    public ServiceOrderPo findServiceOrderByMaintainerId(Long maintainerId,Long id) {
        ServiceOrderPo po = serviceOrderDao.findByIdAndMaintainerId(id, maintainerId);
        return po;
    }

    public ReturnObject createServiceOrder(Long shopId, Long aftersaleId, ServiceOrderDto dto) {

        AftersalePo aftersale = aftersaleFeign.findAftersaleByShopId(shopId, aftersaleId);
        if (aftersale == null) {
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        }

        if (serviceOrderDao.existsByAftersaleId(aftersaleId)) {
            return new ReturnObject(ReturnNo.STATE_NOT_ALLOW);
        }

        ServiceOrderPo po = new ServiceOrderPo();

        po.setShopId(shopId);
        po.setAftersaleId(aftersaleId);
        po.setServiceType(dto.getServiceType());
        po.setConsigneeName(dto.getConsignee().getName());
        po.setConsigneeMobile(dto.getConsignee().getMobile());
        po.setConsigneeRegionId(dto.getConsignee().getRegionId());
        po.setConsigneeAddress(dto.getConsignee().getAddress());
        po.setGmtCreate(LocalDateTime.now());
        po.setGmtModified(LocalDateTime.now());

        try {
            serviceOrderDao.insert(po);
        } catch (Exception e) {
            log.error("【服务模块】创建服务单失败", e);
            return new ReturnObject(ReturnNo.SERVICE_ORDER_CREATE_FAIL);
        }
        ServiceOrderVo serviceOrderVo = new ServiceOrderVo();
        serviceOrderVo.setId(po.getId());
        serviceOrderVo.setServiceType(po.getServiceType());
        serviceOrderVo.setConsigneeRegionId(po.getConsigneeRegionId());

        log.info("【服务模块】服务单创建成功，aftersaleId={}", aftersaleId);
        return new ReturnObject(ReturnNo.OK,serviceOrderVo);
    }

    /* =====================================================
     * 商铺分配服务单
     * ===================================================== */
    public ReturnObject assignServiceOrder(Long shopId, Long id) {
        try {
            ServiceOrderPo order = serviceOrderDao.findByIdAndShopId(id, shopId);
            if (order == null) {
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }

            if (!ServiceOrderStatus.WAITING.same(order)) {
                return new ReturnObject(ReturnNo.STATE_NOT_ALLOW);
            }

            if (order.getConsigneeRegionId() == null || order.getServiceType() == null) {
                return new ReturnObject(ReturnNo.FIELD_NOTVALID);
            }

            ReturnObject contractResult =
                    serviceContractService.findToAssign(
                            shopId,
                            order.getConsigneeRegionId().longValue(),
                            order.getServiceType());

            if (contractResult.getErrno() != ReturnNo.OK.getErrNo()) {
                return contractResult;
            }

            ServiceContractPo contract = (ServiceContractPo) contractResult.getData();
            MaintainerPo maintainer = maintainerDao.findById(contract.getMaintainerId());
            if (maintainer == null) {
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }

            order.setMaintainerId(maintainer.getId());
            order.setMaintainerName(maintainer.getMaintainerName());
            order.setMaintainerMobile(maintainer.getMaintainerMobile());
            order.setMaintainerAddress(maintainer.getMaintainerAddress());
            order.setStatus(ServiceOrderStatus.ASSIGNED.getCode());

            serviceOrderDao.insert(order);
            return new ReturnObject(ReturnNo.OK, order);

        } catch (Exception e) {
            log.error("【服务模块】分配服务单异常", e);
            return new ReturnObject(ReturnNo.SERVICE_ORDER_CREATE_FAIL);
        }
    }

    /* =====================================================
     * 维修人员接单（多态入口）
     * ===================================================== */
    public ReturnObject acceptServiceOrder(Long maintainerId, Long id) {
        try {
            ServiceOrderPo order = findByMaintainer(maintainerId, id);
            if (order == null) {
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }

            ServiceTypeHandler handler =
                    handlerFactory.get(order.getServiceType());

            handler.accept(order);

            serviceOrderDao.insert(order);
            return new ReturnObject(ReturnNo.OK, order);


        } catch (Exception e) {
            log.error("【服务模块】接单异常", e);
            return new ReturnObject(ReturnNo.SERVICE_ORDER_CREATE_FAIL);
        }
    }

    /* =====================================================
     * 维修人员拒单（通用）
     * ===================================================== */
    public ReturnObject refuseServiceOrder(Long maintainerId, Long id) {
        try {
            ServiceOrderPo order = findByMaintainer(maintainerId, id);
            if (order == null) {
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }

            if (!ServiceOrderStatus.ASSIGNED.same(order)) {
                return new ReturnObject(ReturnNo.STATE_NOT_ALLOW);
            }

            ServiceTypeHandler handler =
                    handlerFactory.get(order.getServiceType());

            handler.refuse(order);

            serviceOrderDao.insert(order);
            return new ReturnObject(ReturnNo.OK, order);

        } catch (Exception e) {
            log.error("【服务模块】拒单异常", e);
            return new ReturnObject(ReturnNo.SERVICE_ORDER_CREATE_FAIL);
        }
    }

    /* =====================================================
     * 寄件服务：接收产品
     * ===================================================== */
    public ReturnObject receiveProduct(Long maintainerId, Long id) {
        try {
            ServiceOrderPo order = findByMaintainer(maintainerId, id);
            if (order == null) {
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }

            ServiceTypeHandler handler =
                    handlerFactory.get(order.getServiceType());

            handler.receiveProduct(order);

            serviceOrderDao.insert(order);
            return new ReturnObject(ReturnNo.OK, order);


        } catch (Exception e) {
            log.error("【服务模块】接收产品异常", e);
            return new ReturnObject(ReturnNo.SERVICE_ORDER_CREATE_FAIL);
        }
    }

    /* =====================================================
     * 寄件服务：发送产品
     * ===================================================== */
    public ReturnObject sendProduct(Long maintainerId, Long id) {
        try {
            ServiceOrderPo order = findByMaintainer(maintainerId, id);
            if (order == null) {
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }

            ServiceTypeHandler handler =
                    handlerFactory.get(order.getServiceType());

            handler.sendProduct(order);

            serviceOrderDao.insert(order);
            return new ReturnObject(ReturnNo.OK, order);


        } catch (Exception e) {
            log.error("【服务模块】发送产品异常", e);
            return new ReturnObject(ReturnNo.SERVICE_ORDER_CREATE_FAIL);
        }
    }

    /* =====================================================
     * 完成服务单（多态校验）
     * ===================================================== */
    public ReturnObject finishServiceOrder(Long maintainerId, Long id) {
        try {
            ServiceOrderPo order = findByMaintainer(maintainerId, id);
            if (order == null) {
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }

            ServiceTypeHandler handler =
                    handlerFactory.get(order.getServiceType());

            handler.finish(order);
            serviceOrderDao.insert(order);

            syncAftersale(order);

            return new ReturnObject(ReturnNo.OK, order);


        } catch (Exception e) {
            log.error("【服务模块】完成服务单异常", e);
            return new ReturnObject(ReturnNo.SERVICE_ORDER_CREATE_FAIL);
        }
    }

    /* =====================================================
     * 私有工具方法
     * ===================================================== */

    private ServiceOrderPo findByMaintainer(Long maintainerId, Long id) {
        return serviceOrderDao.findByIdAndMaintainerId(id, maintainerId);
    }

    private void syncAftersale(ServiceOrderPo order) {
        if (order.getAftersaleId() == null || order.getShopId() == null) {
            return;
        }
        try {
            aftersaleFeign.updateAftersaleStatus(
                    order.getShopId(),
                    order.getAftersaleId(),
                    AftersaleStatus.COMPLETED
            );
        } catch (Exception e) {
            log.error("【服务模块】同步售后状态失败，aftersaleId={}",
                    order.getAftersaleId(), e);
        }
    }
}



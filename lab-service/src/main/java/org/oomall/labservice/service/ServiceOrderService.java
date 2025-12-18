package org.oomall.labservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.oomall.labservice.controller.dto.ServiceOrderDto;

import org.oomall.labservice.dao.ServiceOrderDao;
import org.oomall.labservice.feign.AftersaleFeign;
import org.oomall.labservice.feign.dto.AftersaleDto;
import org.oomall.labservice.feign.po.AftersalePo;
import org.oomall.labservice.mapper.Po.ServiceOrderPo;

import org.oomall.labservice.model.ReturnNo;
import org.oomall.labservice.model.ReturnObject;

import org.oomall.labservice.service.vo.ServiceOrderVo;
import org.springframework.stereotype.Service;


@Slf4j
@RequiredArgsConstructor
@Service
public class ServiceOrderService {
    private final ServiceOrderDao serviceOrderDao;
    private final AftersaleFeign aftersaleFeign;
    public ReturnObject createServiceOrder(Long shopId, Long aftersaleId, ServiceOrderDto vo) {

        AftersalePo aftersale = aftersaleFeign.getAftersale(shopId, aftersaleId);
        if (aftersale == null) {
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        }



        if (serviceOrderDao.existsByAftersaleId(aftersaleId)) {
            return new ReturnObject(ReturnNo.STATE_NOT_ALLOW);
        }

        ServiceOrderPo po = new ServiceOrderPo();
        po.setShopId(shopId);
        po.setAftersaleId(aftersaleId);
        po.setType(0); // 默认上门，可根据需求改
        po.setConsigneeName(vo.getConsignee().getName());
        po.setConsigneeMobile(vo.getConsignee().getMobile());
        po.setConsigneeRegionId(vo.getConsignee().getRegionId());
        po.setConsigneeAddress(vo.getConsignee().getAddress());

        try {
            serviceOrderDao.insert(po);
        } catch (Exception e) {
            log.error("【服务模块】创建服务单失败", e);
            return new ReturnObject(ReturnNo.SERVICE_ORDER_CREATE_FAIL);
        }
        ServiceOrderVo serviceOrderVo = new ServiceOrderVo();
        serviceOrderVo.setId(po.getId());
        serviceOrderVo.setType(po.getType());
        serviceOrderVo.setConsignee(vo.getConsignee());

        log.info("【服务模块】服务单创建成功，aftersaleId={}", aftersaleId);
        return new ReturnObject(ReturnNo.OK,serviceOrderVo);
    }
}

package org.oomall.labservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oomall.labaftersale.controller.dto.AftersaleDto;
import org.oomall.labservice.controller.dto.SimpleService;
import org.oomall.labservice.dao.ServiceOrderDao;
import org.oomall.labservice.feign.AftersaleFeign;
import org.oomall.labservice.mapper.Po.ServiceOrderPo;
import org.oomall.labservice.mapper.ServiceOrderMapper;
import org.oomall.labservice.model.ReturnNo;
import org.oomall.labservice.model.ReturnObject;
import org.springframework.stereotype.Service;


@Slf4j
@RequiredArgsConstructor
@Service
public class ServiceOrderService {
    private final ServiceOrderDao serviceOrderDao;
    private final AftersaleFeign aftersaleFeign;
    public ReturnObject createServiceOrder(Long shopId, Long aftersaleId) {

        AftersaleDto aftersale = aftersaleFeign.getAftersale(aftersaleId);
        if (aftersale == null) {
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        }

        if (!"REPAIR".equals(aftersale.getType().name())) {
            return new ReturnObject(ReturnNo.AFTERSALE_TYPE_ERROR);
        }

        if (serviceOrderDao.existsByAftersaleId(aftersaleId)) {
            return new ReturnObject(ReturnNo.STATE_NOT_ALLOW);
        }

        ServiceOrderPo po = new ServiceOrderPo();
        po.setShopId(shopId);
        po.setAftersaleId(aftersaleId);
        po.setType(0); // 默认上门，可根据需求改
        po.setConsignee("默认联系人"); // TODO 可扩展成请求体字段

        try {
            serviceOrderDao.insert(po);
        } catch (Exception e) {
            log.error("【服务模块】创建服务单失败", e);
            return new ReturnObject(ReturnNo.SERVICE_ORDER_CREATE_FAIL);
        }

        SimpleService simple = new SimpleService();
        simple.setId(po.getId());
        simple.setType(po.getType());
        simple.setConsignee(po.getConsignee());

        log.info("【服务模块】服务单创建成功，aftersaleId={}", aftersaleId);
        return new ReturnObject(simple);
    }
}

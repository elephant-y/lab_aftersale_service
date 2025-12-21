package org.oomall.labservice.service;



import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oomall.labservice.controller.dto.ServiceOrderDto;
import org.oomall.labservice.dao.MaintainerDao;
import org.oomall.labservice.dao.ServiceOrderDao;
import org.oomall.labservice.feign.AftersaleFeign;
import org.oomall.labservice.feign.po.AftersalePo;
import org.oomall.labservice.mapper.Po.Consignee;
import org.oomall.labservice.mapper.Po.MaintainerPo;
import org.oomall.labservice.mapper.Po.ServiceContractPo;
import org.oomall.labservice.mapper.Po.ServiceOrderPo;
import org.oomall.labservice.model.ReturnNo;
import org.oomall.labservice.model.ReturnObject;
import org.oomall.labservice.model.ServiceOrderStatus;
import org.oomall.labservice.model.ServiceOrderType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOrderServiceTest {

    @InjectMocks
    private ServiceOrderService serviceOrderService;

    @Mock
    private ServiceOrderDao serviceOrderDao;

    @Mock
    private AftersaleFeign aftersaleFeign;

    @Mock
    private ServiceContractService serviceContractService;

    @Mock
    private MaintainerDao maintainerDao;

    /* ========================= 创建服务单 ========================= */

    @Test
    void createServiceOrder_success() {
        Long shopId = 1L;
        Long aftersaleId = 10L;

        ServiceOrderDto dto = new ServiceOrderDto();
        dto.setServiceType(ServiceOrderType.SITEMENT.ordinal());

        Consignee consignee = new Consignee();
        consignee.setName("张三");
        consignee.setMobile("13800000000");
        consignee.setRegionId(100);
        consignee.setAddress("北京");
        dto.setConsignee(consignee);

        when(aftersaleFeign.getAftersale(shopId, aftersaleId))
                .thenReturn(new AftersalePo());
        when(serviceOrderDao.existsByAftersaleId(aftersaleId))
                .thenReturn(false);

        ReturnObject result =
                serviceOrderService.createServiceOrder(shopId, aftersaleId, dto);

        assertEquals(0, result.getErrno());
        verify(serviceOrderDao).insert(any(ServiceOrderPo.class));
    }

    @Test
    void createServiceOrder_aftersaleNotExist() {
        when(aftersaleFeign.getAftersale(anyLong(), anyLong()))
                .thenReturn(null);

        ReturnObject result =
                serviceOrderService.createServiceOrder(1L, 1L, new ServiceOrderDto());

        assertEquals(504, result.getErrno());
    }

    /* ========================= 分配服务单 ========================= */

    @Test
    void assignServiceOrder_success() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setId(1L);
        order.setShopId(1L);
        order.setStatus(ServiceOrderStatus.WAITING.ordinal());
        order.setConsigneeRegionId(100);
        order.setServiceType(ServiceOrderType.SITEMENT.ordinal());

        ServiceContractPo contract = new ServiceContractPo();
        contract.setMaintainerId(10L);

        MaintainerPo maintainer = new MaintainerPo();
        maintainer.setId(10L);
        maintainer.setMaintainerName("李四");

        when(serviceOrderDao.findByIdAndShopId(1L, 1L))
                .thenReturn(order);
        when(serviceContractService.findToAssign(anyLong(), anyLong(), anyInt()))
                .thenReturn(new ReturnObject(ReturnNo.OK, contract));
        when(maintainerDao.findById(10L))
                .thenReturn(maintainer);

        ReturnObject result =
                serviceOrderService.assignServiceOrder(1L, 1L);

        assertEquals(0, result.getErrno());
        assertEquals(ServiceOrderStatus.ASSIGNED.ordinal(), order.getStatus());
    }

    @Test
    void assignServiceOrder_stateNotAllow() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setStatus(ServiceOrderStatus.ASSIGNED.ordinal());

        when(serviceOrderDao.findByIdAndShopId(anyLong(), anyLong()))
                .thenReturn(order);

        ReturnObject result =
                serviceOrderService.assignServiceOrder(1L, 1L);

        assertEquals(507, result.getErrno());
    }

    /* ========================= 接单 / 拒单 ========================= */

    @Test
    void acceptServiceOrder_success() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setId(1L);
        order.setMaintainerId(2L);
        order.setServiceType(ServiceOrderType.SITEMENT.ordinal());
        order.setStatus(ServiceOrderStatus.ASSIGNED.ordinal());

        when(serviceOrderDao.findByIdAndMaintainerId(1L, 2L))
                .thenReturn(order);

        ReturnObject result =
                serviceOrderService.acceptServiceOrder(2L, 1L);

        assertEquals(0, result.getErrno());
        assertEquals(ServiceOrderStatus.ACCEPTED.ordinal(), order.getStatus());
    }

    @Test
    void refuseServiceOrder_success() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setStatus(ServiceOrderStatus.ASSIGNED.ordinal());

        when(serviceOrderDao.findByIdAndMaintainerId(anyLong(), anyLong()))
                .thenReturn(order);

        ReturnObject result =
                serviceOrderService.refuseServiceOrder(2L, 1L);

        assertEquals(0, result.getErrno());
        assertEquals(ServiceOrderStatus.REJECTED.ordinal(), order.getStatus());
    }

    /* ========================= 寄件服务 ========================= */

    @Test
    void receiveProduct_success() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setServiceType(ServiceOrderType.SHIPMENT.ordinal());
        order.setStatus(ServiceOrderStatus.ACCEPTED.ordinal());

        when(serviceOrderDao.findByIdAndMaintainerId(anyLong(), anyLong()))
                .thenReturn(order);

        ReturnObject result =
                serviceOrderService.receiveProduct(2L, 1L);

        assertEquals(0, result.getErrno());
        assertEquals(ServiceOrderStatus.RECEIVED.ordinal(), order.getStatus());
    }

    @Test
    void sendProduct_success() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setServiceType(ServiceOrderType.SHIPMENT.ordinal());
        order.setStatus(ServiceOrderStatus.RECEIVED.ordinal());

        when(serviceOrderDao.findByIdAndMaintainerId(anyLong(), anyLong()))
                .thenReturn(order);

        ReturnObject result =
                serviceOrderService.sendProduct(2L, 1L);

        assertEquals(0, result.getErrno());
        assertEquals(ServiceOrderStatus.SENT.ordinal(), order.getStatus());
    }

    /* ========================= 完成服务单 ========================= */

    @Test
    void finishServiceOrder_siteService_success() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setServiceType(ServiceOrderType.SITEMENT.ordinal());
        order.setStatus(ServiceOrderStatus.ACCEPTED.ordinal());
        order.setShopId(1L);
        order.setAftersaleId(10L);

        when(serviceOrderDao.findByIdAndMaintainerId(anyLong(), anyLong()))
                .thenReturn(order);

        ReturnObject result =
                serviceOrderService.finishServiceOrder(2L, 1L);

        assertEquals(0, result.getErrno());
        assertEquals(ServiceOrderStatus.FINISHED.ordinal(), order.getStatus());
    }

    @Test
    void finishServiceOrder_stateNotAllow() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setServiceType(ServiceOrderType.SITEMENT.ordinal());
        order.setStatus(ServiceOrderStatus.WAITING.ordinal());

        when(serviceOrderDao.findByIdAndMaintainerId(anyLong(), anyLong()))
                .thenReturn(order);

        ReturnObject result =
                serviceOrderService.finishServiceOrder(2L, 1L);

        assertEquals(507, result.getErrno());
    }
}
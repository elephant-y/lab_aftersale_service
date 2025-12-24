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

    /* ========================= 查询服务单 ========================= */
    @Test
    void findServiceOrderByShopId_success() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setId(1L);
        order.setShopId(1L);

        when(serviceOrderDao.findByIdAndShopId(anyLong(), anyLong()))
                .thenReturn(order);

        ServiceOrderPo result =
                serviceOrderService.findServiceOrderByShopId(1L, 1L);

        assertEquals(order, result);
    }

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

        when(aftersaleFeign.findAftersaleByShopId(shopId, aftersaleId))
                .thenReturn(new AftersalePo());
        when(serviceOrderDao.existsByAftersaleId(aftersaleId))
                .thenReturn(false);

        ReturnObject result =
                serviceOrderService.createServiceOrder(shopId, aftersaleId, dto);

        assertEquals(0, result.getErrno());
        verify(serviceOrderDao).save(any(ServiceOrderPo.class));
    }

    @Test
    void createServiceOrder_aftersaleNotExist() {
        when(aftersaleFeign.findAftersaleByShopId(anyLong(), anyLong()))
                .thenReturn(null);

        ReturnObject result =
                serviceOrderService.createServiceOrder(1L, 1L, new ServiceOrderDto());

        assertEquals(504, result.getErrno());
    }

    @Test
    void createServiceOrder_aftersaleHasServiceOrder() {
        when(aftersaleFeign.findAftersaleByShopId(anyLong(), anyLong()))
                .thenReturn(new AftersalePo());
        when(serviceOrderDao.existsByAftersaleId(anyLong()))
                .thenReturn(true);

        ReturnObject result =
                serviceOrderService.createServiceOrder(1L, 1L, new ServiceOrderDto());

        assertEquals(508, result.getErrno());
    }

    @Test
    public void createServiceOrder_whenInsertFails() {
        // Given
        Long shopId = 1L;
        Long aftersaleId = 1L;
        ServiceOrderDto dto = new ServiceOrderDto();
        dto.setServiceType(1);
        dto.setConsignee(new Consignee());

        AftersalePo mockAftersale = new AftersalePo();
        mockAftersale.setId(aftersaleId);

        // Mock aftersaleFeign返回有效的售后单
        when(aftersaleFeign.findAftersaleByShopId(shopId, aftersaleId))
                .thenReturn(mockAftersale);

        // Mock serviceOrderDao.existsByAftersaleId返回false（表示不存在）
        when(serviceOrderDao.existsByAftersaleId(aftersaleId))
                .thenReturn(false);

        // Mock serviceOrderDao.save抛出异常
        doThrow(new RuntimeException("Database error"))
                .when(serviceOrderDao).save(any(ServiceOrderPo.class));

        // When
        ReturnObject result = serviceOrderService.createServiceOrder(shopId, aftersaleId, dto);

        // Then
        assertEquals(ReturnNo.SERVICE_ORDER_CREATE_FAIL.getErrNo(), result.getErrno());
        verify(serviceOrderDao).save(any(ServiceOrderPo.class));
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
    void assignServiceOrder_serviceOrderNotExist() {
        when(serviceOrderDao.findByIdAndShopId(anyLong(), anyLong()))
                .thenReturn(null);

        ReturnObject result =
                serviceOrderService.assignServiceOrder(1L, 1L);

        assertEquals(504, result.getErrno());
    }

    @Test
    void assignServiceOrder_serviceTypeIsMiss() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setId(1L);
        order.setShopId(1L);
        order.setStatus(ServiceOrderStatus.WAITING.ordinal());
        order.setConsigneeRegionId(100);
        order.setServiceType(null);

        when(serviceOrderDao.findByIdAndShopId(1L, 1L))
                .thenReturn(order);

        ReturnObject result =
                serviceOrderService.assignServiceOrder(1L, 1L);

        assertEquals(503, result.getErrno());
    }

    @Test
    void assignServiceOrder_serviceContractNotExist() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setId(1L);
        order.setShopId(1L);
        order.setStatus(ServiceOrderStatus.WAITING.ordinal());
        order.setConsigneeRegionId(100);
        order.setServiceType(ServiceOrderType.SITEMENT.ordinal());

        when(serviceOrderDao.findByIdAndShopId(1L, 1L))
                .thenReturn(order);
        when(serviceContractService.findToAssign(anyLong(), anyLong(), anyInt()))
                .thenReturn(new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST, null));

        ReturnObject result =
                serviceOrderService.assignServiceOrder(1L, 1L);

        assertEquals(504, result.getErrno());
    }

    @Test
    void assignServiceOrder_maintainerNotExist() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setId(1L);
        order.setShopId(1L);
        order.setStatus(ServiceOrderStatus.WAITING.ordinal());
        order.setConsigneeRegionId(100);
        order.setServiceType(ServiceOrderType.SITEMENT.ordinal());

        ServiceContractPo contract = new ServiceContractPo();
        contract.setMaintainerId(10L);

        when(serviceOrderDao.findByIdAndShopId(1L, 1L))
                .thenReturn(order);
        when(serviceContractService.findToAssign(anyLong(), anyLong(), anyInt()))
                .thenReturn(new ReturnObject(ReturnNo.OK, contract));
        when(maintainerDao.findById(10L))
                .thenReturn(null);

        ReturnObject result =
                serviceOrderService.assignServiceOrder(1L, 1L);

        assertEquals(504, result.getErrno());
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

    @Test
    void assignServiceOrder_whenExceptionOccurs() {
        // Given
        Long shopId = 1L;
        Long orderId = 1L;
        ServiceOrderPo mockOrder = new ServiceOrderPo();
        mockOrder.setStatus(ServiceOrderStatus.WAITING.ordinal());

        // Mock serviceOrderDao.findByIdAndShopId to throw exception
        when(serviceOrderDao.findByIdAndShopId(orderId, shopId))
                .thenThrow(new RuntimeException("Database error"));

        // When
        ReturnObject result = serviceOrderService.assignServiceOrder(shopId, orderId);

        // Then
        assertEquals(ReturnNo.SERVICE_ORDER_CREATE_FAIL.getErrNo(), result.getErrno());
    }

    /* ========================= 接单 / 拒单 ========================= */

    @Test
    void acceptServiceOrder_success_site() {
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
    void acceptServiceOrder_success_ship() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setId(1L);
        order.setMaintainerId(2L);
        order.setServiceType(ServiceOrderType.SHIPMENT.ordinal());
        order.setStatus(ServiceOrderStatus.ASSIGNED.ordinal());

        when(serviceOrderDao.findByIdAndMaintainerId(1L, 2L))
                .thenReturn(order);

        ReturnObject result =
                serviceOrderService.acceptServiceOrder(2L, 1L);

        assertEquals(0, result.getErrno());
        assertEquals(ServiceOrderStatus.ACCEPTED.ordinal(), order.getStatus());
    }

    @Test
    void acceptServiceOrder_serviceOrderNotExist() {
        when(serviceOrderDao.findByIdAndMaintainerId(anyLong(), anyLong()))
                .thenReturn(null);

        ReturnObject result =
                serviceOrderService.acceptServiceOrder(2L, 1L);

        assertEquals(504, result.getErrno());
    }

    @Test
    void acceptServiceOrder_stateNotAllow() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setId(1L);
        order.setMaintainerId(2L);
        order.setServiceType(ServiceOrderType.SITEMENT.ordinal());
        order.setStatus(ServiceOrderStatus.ACCEPTED.ordinal());

        when(serviceOrderDao.findByIdAndMaintainerId(1L, 2L))
                .thenReturn(order);

        ReturnObject result =
                serviceOrderService.acceptServiceOrder(2L, 1L);

        assertEquals(507, result.getErrno());
    }

    @Test
    void acceptServiceOrder_serviceOrderNotMatch() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setId(1L);
        order.setMaintainerId(2L);
        order.setServiceType(2);
        order.setStatus(ServiceOrderStatus.ASSIGNED.ordinal());

        when(serviceOrderDao.findByIdAndMaintainerId(1L, 2L))
                .thenReturn(order);

        ReturnObject result =
                serviceOrderService.acceptServiceOrder(2L, 1L);

        assertEquals(803, result.getErrno());
    }

    @Test
    void acceptServiceOrder_whenExceptionOccurs() {
        // Given
        Long maintainerId = 1L;
        Long orderId = 1L;
        ServiceOrderPo mockOrder = new ServiceOrderPo();
        mockOrder.setStatus(ServiceOrderStatus.ASSIGNED.ordinal());

        // Mock serviceOrderDao.findByIdAndMaintainerId to throw exception
        when(serviceOrderDao.findByIdAndMaintainerId(orderId, maintainerId))
                .thenThrow(new RuntimeException("Database error"));

        // When
        ReturnObject result = serviceOrderService.acceptServiceOrder(maintainerId, orderId);

        // Then
        assertEquals(ReturnNo.SERVICE_ORDER_CREATE_FAIL.getErrNo(), result.getErrno());
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

    @Test
    void refuseServiceOrder_serviceOrderNotExist() {
        when(serviceOrderDao.findByIdAndMaintainerId(anyLong(), anyLong()))
                .thenReturn(null);

        ReturnObject result =
                serviceOrderService.refuseServiceOrder(2L, 1L);

        assertEquals(504, result.getErrno());
    }

    @Test
    void refuseServiceOrder_stateNotAllow() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setStatus(ServiceOrderStatus.ACCEPTED.ordinal());

        when(serviceOrderDao.findByIdAndMaintainerId(anyLong(), anyLong()))
                .thenReturn(order);

        ReturnObject result =
                serviceOrderService.refuseServiceOrder(2L, 1L);

        assertEquals(507, result.getErrno());
    }

    @Test
    public void refuseServiceOrder_whenExceptionOccurs() {
        // Given
        Long maintainerId = 1L;
        Long orderId = 1L;
        ServiceOrderPo mockOrder = new ServiceOrderPo();
        mockOrder.setStatus(ServiceOrderStatus.ASSIGNED.ordinal());

        // Mock serviceOrderDao.findByIdAndMaintainerId to throw exception
        when(serviceOrderDao.findByIdAndMaintainerId(orderId, maintainerId))
                .thenThrow(new RuntimeException("Database error"));

        // When
        ReturnObject result = serviceOrderService.refuseServiceOrder(maintainerId, orderId);

        // Then
        assertEquals(ReturnNo.SERVICE_ORDER_CREATE_FAIL.getErrNo(), result.getErrno());
        verify(serviceOrderDao, never()).save(any());
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
    void receiveProduct_serviceOrderNotExist() {
        when(serviceOrderDao.findByIdAndMaintainerId(anyLong(), anyLong()))
                .thenReturn(null);

        ReturnObject result =
                serviceOrderService.receiveProduct(2L, 1L);

        assertEquals(504, result.getErrno());
    }

    @Test
    void receiveProduct_serviceTypeNotMatch() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setServiceType(ServiceOrderType.SITEMENT.ordinal());
        order.setStatus(ServiceOrderStatus.ACCEPTED.ordinal());

        when(serviceOrderDao.findByIdAndMaintainerId(anyLong(), anyLong()))
                .thenReturn(order);

        ReturnObject result =
                serviceOrderService.receiveProduct(2L, 1L);

        assertEquals(803, result.getErrno());
    }

    @Test
    void receiveProduct_stateNotAllow() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setServiceType(ServiceOrderType.SHIPMENT.ordinal());
        order.setStatus(ServiceOrderStatus.WAITING.ordinal());

        when(serviceOrderDao.findByIdAndMaintainerId(anyLong(), anyLong()))
                .thenReturn(order);

        ReturnObject result =
                serviceOrderService.receiveProduct(2L, 1L);

        assertEquals(507, result.getErrno());
    }

    @Test
    void receiveProduct_whenExceptionOccurs() {
        // Given
        Long maintainerId = 1L;
        Long orderId = 1L;
        ServiceOrderPo mockOrder = new ServiceOrderPo();
        mockOrder.setServiceType(ServiceOrderType.SHIPMENT.ordinal());
        mockOrder.setStatus(ServiceOrderStatus.ACCEPTED.ordinal());

        // Mock serviceOrderDao.findByIdAndMaintainerId to throw exception
        when(serviceOrderDao.findByIdAndMaintainerId(orderId, maintainerId))
                .thenThrow(new RuntimeException("Database error"));

        // When
        ReturnObject result = serviceOrderService.receiveProduct(maintainerId, orderId);

        // Then
        assertEquals(ReturnNo.SERVICE_ORDER_CREATE_FAIL.getErrNo(), result.getErrno());
        verify(serviceOrderDao, never()).save(any());
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

    @Test
    void sendProduct_serviceOrderNotExist() {
        when(serviceOrderDao.findByIdAndMaintainerId(anyLong(), anyLong()))
                .thenReturn(null);

        ReturnObject result =
                serviceOrderService.sendProduct(2L, 1L);

        assertEquals(504, result.getErrno());
    }

    @Test
    void sendProduct_serviceTypeNotMatch() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setServiceType(ServiceOrderType.SITEMENT.ordinal());
        order.setStatus(ServiceOrderStatus.RECEIVED.ordinal());

        when(serviceOrderDao.findByIdAndMaintainerId(anyLong(), anyLong()))
                .thenReturn(order);

        ReturnObject result =
                serviceOrderService.sendProduct(2L, 1L);

        assertEquals(803, result.getErrno());
    }

    @Test
    void sendProduct_stateNotAllow() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setServiceType(ServiceOrderType.SHIPMENT.ordinal());
        order.setStatus(ServiceOrderStatus.WAITING.ordinal());

        when(serviceOrderDao.findByIdAndMaintainerId(anyLong(), anyLong()))
                .thenReturn(order);

        ReturnObject result =
                serviceOrderService.sendProduct(2L, 1L);

        assertEquals(507, result.getErrno());
    }

    @Test
    void sendProduct_whenExceptionOccurs() {
        // Given
        Long maintainerId = 1L;
        Long orderId = 1L;
        ServiceOrderPo mockOrder = new ServiceOrderPo();
        mockOrder.setServiceType(ServiceOrderType.SHIPMENT.ordinal());
        mockOrder.setStatus(ServiceOrderStatus.RECEIVED.ordinal());

        // Mock serviceOrderDao.findByIdAndMaintainerId to throw exception
        when(serviceOrderDao.findByIdAndMaintainerId(orderId, maintainerId))
                .thenThrow(new RuntimeException("Database error"));

        // When
        ReturnObject result = serviceOrderService.sendProduct(maintainerId, orderId);

        // Then
        assertEquals(ReturnNo.SERVICE_ORDER_CREATE_FAIL.getErrNo(), result.getErrno());
        verify(serviceOrderDao, never()).save(any());
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
    void finishServiceOrder_serviceOrderNotExist() {
        when(serviceOrderDao.findByIdAndMaintainerId(anyLong(), anyLong()))
                .thenReturn(null);

        ReturnObject result =
                serviceOrderService.finishServiceOrder(2L, 1L);

        assertEquals(504, result.getErrno());
    }

    @Test
    void finishServiceOrder_serviceTypeNotMatch() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setServiceType(2);
        order.setStatus(ServiceOrderStatus.ACCEPTED.ordinal());

        when(serviceOrderDao.findByIdAndMaintainerId(anyLong(), anyLong()))
                .thenReturn(order);

        ReturnObject result =
                serviceOrderService.finishServiceOrder(2L, 1L);

        assertEquals(803, result.getErrno());
    }

    @Test
    void finishServiceOrder_siteService_stateNotAllow() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setServiceType(ServiceOrderType.SITEMENT.ordinal());
        order.setStatus(ServiceOrderStatus.WAITING.ordinal());

        when(serviceOrderDao.findByIdAndMaintainerId(anyLong(), anyLong()))
                .thenReturn(order);

        ReturnObject result =
                serviceOrderService.finishServiceOrder(2L, 1L);

        assertEquals(507, result.getErrno());
    }

    @Test
    void finishServiceOrder_shipmentService_success() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setServiceType(ServiceOrderType.SHIPMENT.ordinal());
        order.setStatus(ServiceOrderStatus.SENT.ordinal());
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
    void finishServiceOrder_aftersaleIdIsNull() {
        ServiceOrderPo order = new ServiceOrderPo();
        order.setServiceType(ServiceOrderType.SHIPMENT.ordinal());
        order.setStatus(ServiceOrderStatus.SENT.ordinal());
        order.setShopId(1L);
        order.setAftersaleId(null);

        when(serviceOrderDao.findByIdAndMaintainerId(anyLong(), anyLong()))
                .thenReturn(order);

        ReturnObject result =
                serviceOrderService.finishServiceOrder(2L, 1L);

        assertEquals(0, result.getErrno());
    }

    @Test
    void finishServiceOrder_whenExceptionOccurs() {
        // Given
        Long maintainerId = 1L;
        Long orderId = 1L;
        ServiceOrderPo mockOrder = new ServiceOrderPo();
        mockOrder.setServiceType(ServiceOrderType.SHIPMENT.ordinal());
        mockOrder.setStatus(ServiceOrderStatus.SENT.ordinal());

        // Mock serviceOrderDao.findByIdAndMaintainerId to throw exception
        when(serviceOrderDao.findByIdAndMaintainerId(orderId, maintainerId))
                .thenThrow(new RuntimeException("Database error"));

        // When
        ReturnObject result = serviceOrderService.finishServiceOrder(maintainerId, orderId);

        // Then
        assertEquals(ReturnNo.SERVICE_ORDER_CREATE_FAIL.getErrNo(), result.getErrno());
        verify(serviceOrderDao, never()).save(any());
    }
}

package org.oomall.labservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oomall.labservice.controller.dto.ServiceOrderDto;
import org.oomall.labservice.mapper.Po.ServiceOrderPo;
import org.oomall.labservice.model.ReturnNo;
import org.oomall.labservice.model.ReturnObject;
import org.oomall.labservice.service.ServiceOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceOrderController.class)  // 只加载 Controller 层
@Slf4j
class ServiceOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;  // 用于 JSON 序列化

    @MockBean
    private ServiceOrderService serviceOrderService;  // Mock Service 层

    private static final Long SHOP_ID = 1L;
    private static final Long AFTERSALE_ID = 1L;
    private static final Long ORDER_ID = 10001L;
    private static final Long MAINTAINER_ID = 2L;

    // 构造一个完整的 ServiceOrderPo 用于测试
    private ServiceOrderPo buildServiceOrderPo() {
        ServiceOrderPo po = new ServiceOrderPo();
        po.setId(ORDER_ID);
        po.setShopId(SHOP_ID);
        po.setAftersaleId(AFTERSALE_ID);
        po.setServiceType(1);
        po.setStatus(0); // 假设 0 是初始状态
        po.setGmtCreate(LocalDateTime.now().minusDays(1));
        po.setGmtModified(LocalDateTime.now());

        po.setConsigneeName("张三");
        po.setConsigneeMobile("13800000000");
        po.setConsigneeRegionId(123);
        po.setConsigneeAddress("北京市朝阳区");

        po.setMaintainerId(MAINTAINER_ID);
        po.setMaintainerName("李四");
        po.setMaintainerMobile("13900000000");
        po.setMaintainerAddress("上海市浦东新区");

        return po;
    }

    @Test
    @DisplayName("创建服务单 - 成功调用 Service 层")
    void createServiceOrder_shouldCallService() throws Exception {
        ServiceOrderDto dto = new ServiceOrderDto();
        dto.setServiceType(1);

        ReturnObject mockReturn = new ReturnObject(ReturnNo.OK);
        when(serviceOrderService.createServiceOrder(eq(SHOP_ID), eq(AFTERSALE_ID), any(ServiceOrderDto.class)))
                .thenReturn(mockReturn);

        mockMvc.perform(post("/internal/shops/{shopId}/aftersales/{id}/serviceorders", SHOP_ID, AFTERSALE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()));

        verify(serviceOrderService).createServiceOrder(eq(SHOP_ID), eq(AFTERSALE_ID), any());
    }

    @Test
    @DisplayName("按店铺查询服务单 - 成功返回详情")
    void findServiceOrderByShopId_shouldReturnDetail_whenFound() throws Exception {
        ServiceOrderPo po = buildServiceOrderPo();
        when(serviceOrderService.findServiceOrderByShopId(SHOP_ID, ORDER_ID)).thenReturn(po);

        mockMvc.perform(get("/internal/shops/{shopId}/serviceorders/{orderId}", SHOP_ID, ORDER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(0))
                .andExpect(jsonPath("$.data.id").value(ORDER_ID.intValue()))
                .andExpect(jsonPath("$.data.shopId").value(SHOP_ID.intValue()))
                .andExpect(jsonPath("$.data.consignee.consigneeName").value("张三"))
                .andExpect(jsonPath("$.data.maintainer.maintainerName").value("李四"));
    }

    @Test
    @DisplayName("按店铺查询服务单 - 不存在时返回 504")
    void findServiceOrderByShopId_shouldReturnNotExist_whenNull() throws Exception {
        when(serviceOrderService.findServiceOrderByShopId(SHOP_ID, ORDER_ID)).thenReturn(null);

        mockMvc.perform(get("/internal/shops/{shopId}/serviceorders/{orderId}", SHOP_ID, ORDER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.RESOURCE_ID_NOTEXIST.getErrNo()));
    }

    @Test
    @DisplayName("按维修人员查询服务单 - 成功返回详情")
    void getServiceOrderByMaintainer_shouldReturnDetail_whenFound() throws Exception {
        ServiceOrderPo po = buildServiceOrderPo();
        when(serviceOrderService.findServiceOrderByMaintainerId(MAINTAINER_ID, ORDER_ID)).thenReturn(po);

        mockMvc.perform(get("/internal/maintainers/{maintainerId}/serviceorders/{orderId}", MAINTAINER_ID, ORDER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(ORDER_ID.intValue()))
                .andExpect(jsonPath("$.data.maintainer.id").value(MAINTAINER_ID.intValue()));
    }

    @Test
    @DisplayName("按维修人员查询服务单 - 不存在时返回 504")
    void getServiceOrderByMaintainer_shouldReturnNotExist_whenNull() throws Exception {
        when(serviceOrderService.findServiceOrderByMaintainerId(MAINTAINER_ID, ORDER_ID)).thenReturn(null);

        mockMvc.perform(get("/internal/maintainers/{maintainerId}/serviceorders/{orderId}", MAINTAINER_ID, ORDER_ID))
                .andDo(print())
                .andExpect(jsonPath("$.errno").value(ReturnNo.RESOURCE_ID_NOTEXIST.getErrNo()));
    }

    // 以下是所有 POST 操作的简单成功测试（这些方法基本只有 log + 调用 service，无复杂逻辑）

    @Test
    @DisplayName("分配服务单")
    void assignServiceOrder_shouldCallService() throws Exception {
        ReturnObject mockReturn = new ReturnObject(ReturnNo.OK);
        when(serviceOrderService.assignServiceOrder(SHOP_ID, ORDER_ID)).thenReturn(mockReturn);

        mockMvc.perform(post("/shops/{shopId}/serviceorders/{orderId}/assign", SHOP_ID, ORDER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(0));

        verify(serviceOrderService).assignServiceOrder(SHOP_ID, ORDER_ID);
    }

    @Test
    @DisplayName("维修人员接受服务单")
    void acceptServiceOrder_shouldCallService() throws Exception {
        ReturnObject mockReturn = new ReturnObject(ReturnNo.OK);
        when(serviceOrderService.acceptServiceOrder(MAINTAINER_ID, ORDER_ID)).thenReturn(mockReturn);

        mockMvc.perform(post("/maintainers/{maintainerId}/serviceorders/{orderId}/accept", MAINTAINER_ID, ORDER_ID))
                .andExpect(status().isOk());

        verify(serviceOrderService).acceptServiceOrder(MAINTAINER_ID, ORDER_ID);
    }

    @Test
    @DisplayName("维修人员拒绝服务单")
    void refuseServiceOrder_shouldCallService() throws Exception {
        ReturnObject mockReturn = new ReturnObject(ReturnNo.OK);
        when(serviceOrderService.refuseServiceOrder(MAINTAINER_ID, ORDER_ID)).thenReturn(mockReturn);

        mockMvc.perform(post("/maintainers/{maintainerId}/serviceorders/{orderId}/reject", MAINTAINER_ID, ORDER_ID))
                .andExpect(status().isOk());

        verify(serviceOrderService).refuseServiceOrder(MAINTAINER_ID, ORDER_ID);
    }

    @Test
    @DisplayName("维修人员接收产品")
    void receiveProduct_shouldCallService() throws Exception {
        ReturnObject mockReturn = new ReturnObject(ReturnNo.OK);
        when(serviceOrderService.receiveProduct(MAINTAINER_ID, ORDER_ID)).thenReturn(mockReturn);

        mockMvc.perform(post("/maintainers/{maintainerId}/serviceorders/{orderId}/receive", MAINTAINER_ID, ORDER_ID))
                .andExpect(status().isOk());

        verify(serviceOrderService).receiveProduct(MAINTAINER_ID, ORDER_ID);
    }

    @Test
    @DisplayName("维修人员发送产品")
    void sendProduct_shouldCallService() throws Exception {
        ReturnObject mockReturn = new ReturnObject(ReturnNo.OK);
        when(serviceOrderService.sendProduct(MAINTAINER_ID, ORDER_ID)).thenReturn(mockReturn);

        mockMvc.perform(post("/maintainers/{maintainerId}/serviceorders/{orderId}/send", MAINTAINER_ID, ORDER_ID))
                .andExpect(status().isOk());

        verify(serviceOrderService).sendProduct(MAINTAINER_ID, ORDER_ID);
    }

    @Test
    @DisplayName("维修人员完成服务单")
    void finishServiceOrder_shouldCallService() throws Exception {
        ReturnObject mockReturn = new ReturnObject(ReturnNo.OK);
        when(serviceOrderService.finishServiceOrder(MAINTAINER_ID, ORDER_ID)).thenReturn(mockReturn);

        mockMvc.perform(post("/maintainers/{maintainerId}/serviceorders/{orderId}/finish", MAINTAINER_ID, ORDER_ID))
                .andExpect(status().isOk());

        verify(serviceOrderService).finishServiceOrder(MAINTAINER_ID, ORDER_ID);
    }
}

package org.oomall.labservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oomall.labservice.model.ReturnNo;
import org.oomall.labservice.model.ReturnObject;
import org.oomall.labservice.service.ServiceContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceContractController.class)
@Slf4j
class ServiceContractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceContractService serviceContractService;

    private static final Long SHOP_ID = 100L;
    private static final Long REGION_ID = 200L;
    private static final Integer SERVICE_TYPE = 1;

    @Test
    @DisplayName("查询可分配服务合同 - 正常调用 Service 层并返回结果")
    void findToAssign_shouldReturnServiceResult() throws Exception {
        // 准备 Service 层返回的 mock 对象（这里假设返回成功）
        ReturnObject mockReturnObject = new ReturnObject(ReturnNo.OK, "查询成功", null);

        when(serviceContractService.findToAssign(eq(SHOP_ID), eq(REGION_ID), eq(SERVICE_TYPE)))
                .thenReturn(mockReturnObject);

        mockMvc.perform(get("/internal/shops/{shopId}/service-contracts/assign/{regionId}/{serviceType}",
                        SHOP_ID, REGION_ID, SERVICE_TYPE))
                .andDo(print())  // 打印请求/响应，便于调试
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()))
                .andExpect(jsonPath("$.errmsg").value("查询成功"));

        // 验证 Service 层方法被正确调用一次
        verify(serviceContractService, times(1))
                .findToAssign(SHOP_ID, REGION_ID, SERVICE_TYPE);
    }

    @Test
    @DisplayName("查询可分配服务合同 - Service 返回错误码时也能正确透传")
    void findToAssign_shouldPassThroughError_whenServiceReturnsError() throws Exception {
        ReturnObject mockErrorReturn = new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);

        when(serviceContractService.findToAssign(eq(SHOP_ID), eq(REGION_ID), eq(SERVICE_TYPE)))
                .thenReturn(mockErrorReturn);

        mockMvc.perform(get("/internal/shops/{shopId}/service-contracts/assign/{regionId}/{serviceType}",
                        SHOP_ID, REGION_ID, SERVICE_TYPE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.RESOURCE_ID_NOTEXIST.getErrNo()));

        verify(serviceContractService).findToAssign(SHOP_ID, REGION_ID, SERVICE_TYPE);
    }
}

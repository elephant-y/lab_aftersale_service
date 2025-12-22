package org.oomall.labaftersale.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.oomall.labaftersale.controller.dto.AftersaleApplyDto;
import org.oomall.labaftersale.controller.dto.AftersaleDto;
import org.oomall.labaftersale.controller.dto.AftersaleResponseDto;
import org.oomall.labaftersale.mapper.po.Consignee;
import org.oomall.labaftersale.model.AftersaleType;
import org.oomall.labaftersale.model.ReturnNo;
import org.oomall.labaftersale.model.ReturnObject;
import org.oomall.labaftersale.model.AftersaleStatus;

import org.oomall.labaftersale.service.AftersaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AftersaleController.class)
class AftersaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AftersaleService aftersaleService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 测试：商户审核售后单
     * PUT /shops/{shopId}/aftersales/{id}/confirm
     */
    @Test
    void confirmAftersale_success() throws Exception {
        Long shopId = 1L;
        Long aftersaleId = 10L;

        AftersaleDto dto = new AftersaleDto();
        dto.setConfirm(true);
        dto.setConclusion("同意售后");

        ReturnObject returnObject = new ReturnObject(ReturnNo.OK);

        when(aftersaleService.confirmAftersale(eq(shopId), eq(aftersaleId), any()))
                .thenReturn(returnObject);

        mockMvc.perform(put("/shops/{shopId}/aftersales/{id}/confirm", shopId, aftersaleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()));

        verify(aftersaleService)
                .confirmAftersale(eq(shopId), eq(aftersaleId), any(AftersaleDto.class));
    }

    /**
     * 测试：商户查询售后单（存在）
     * GET /shops/{shopId}/aftersales/{id}
     */
    @Test
    void getAftersale_exist() throws Exception {
        Long shopId = 1L;
        Long aftersaleId = 10L;

        AftersaleResponseDto responseDto = new AftersaleResponseDto();
        responseDto.setId(aftersaleId);

        when(aftersaleService.findAftersaleByShopId(shopId, aftersaleId))
                .thenReturn(responseDto);

        mockMvc.perform(get("/shops/{shopId}/aftersales/{id}", shopId, aftersaleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()))
                .andExpect(jsonPath("$.data.id").value(aftersaleId));
    }

    /**
     * 测试：商户查询售后单（不存在或不属于商户）
     */
    @Test
    void getAftersale_notExist() throws Exception {
        Long shopId = 1L;
        Long aftersaleId = 10L;

        when(aftersaleService.findAftersaleByShopId(shopId, aftersaleId))
                .thenReturn(null);

        mockMvc.perform(get("/shops/{shopId}/aftersales/{id}", shopId, aftersaleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno")
                        .value(ReturnNo.RESOURCE_ID_NOTEXIST.getErrNo()));
    }

    /**
     * 测试：顾客提交售后申请
     * POST /orderitems/{id}/aftersales
     */
    @Test
    void createAftersale_success() throws Exception {
        Long orderItemId = 100L;

        AftersaleApplyDto applyDto = new AftersaleApplyDto();
        applyDto.setReason("商品有质量问题");
        applyDto.setQuantity(1);
        applyDto.setConsignee(new Consignee("张三", "13800138000", 10001L, "北京市朝阳区"));
        applyDto.setType(AftersaleType.RETURN.ordinal());

        ReturnObject returnObject = new ReturnObject(ReturnNo.OK);

        when(aftersaleService.createAftersale(eq(orderItemId), any()))
                .thenReturn(returnObject);

        mockMvc.perform(post("/orderitems/{id}/aftersales", orderItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()));
    }
    /**
     * 测试：更新售后单状态
     * PUT /shops/{shopId}/aftersales/{id}/status
     */
    @Test
    void updateAftersaleStatus_success() throws Exception {
        Long shopId = 1L;
        Long aftersaleId = 10L;

        ReturnObject returnObject = new ReturnObject(ReturnNo.OK);

        when(aftersaleService.updateAftersaleStatus(
                eq(shopId), eq(aftersaleId), eq(AftersaleStatus.COMPLETED)))
                .thenReturn(returnObject);

        mockMvc.perform(put("/shops/{shopId}/aftersales/{id}/status", shopId, aftersaleId)
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()));
    }
}
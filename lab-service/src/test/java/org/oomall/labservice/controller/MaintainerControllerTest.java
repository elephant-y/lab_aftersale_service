package org.oomall.labservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oomall.labservice.mapper.Po.MaintainerPo;
import org.oomall.labservice.model.ReturnNo;
import org.oomall.labservice.model.ReturnObject;
import org.oomall.labservice.service.MaintainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MaintainerController.class)
@Slf4j
class MaintainerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MaintainerService maintainerService;

    private static final Long MAINTAINER_ID = 500L;

    @Test
    @DisplayName("按ID查询维修人员 - Service 返回成功结果")
    void findById_shouldReturnSuccess_whenServiceReturnsOk() throws Exception {

        Map<String, Object> data = new HashMap<>();
        data.put("id", MAINTAINER_ID);
        data.put("name", "张师傅");

        ReturnObject successReturn =
                new ReturnObject(ReturnNo.OK, "查询成功", data);

        when(maintainerService.findById(eq(MAINTAINER_ID)))
                .thenReturn(successReturn);

        mockMvc.perform(get("/internal/maintainers/{id}", MAINTAINER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()))
                .andExpect(jsonPath("$.errmsg").value("查询成功"))
                .andExpect(jsonPath("$.data.id").value(MAINTAINER_ID))
                .andExpect(jsonPath("$.data.name").value("张师傅"));

        verify(maintainerService, times(1)).findById(MAINTAINER_ID);
    }

    @Test
    @DisplayName("按ID查询维修人员 - Service 返回业务错误（如人员不存在）")
    void findById_shouldPassThroughError_whenServiceReturnsError() throws Exception {
        // 模拟 Service 层返回“资源不存在”
        ReturnObject notExistReturn = new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);

        when(maintainerService.findById(eq(MAINTAINER_ID)))
                .thenReturn(notExistReturn);

        mockMvc.perform(get("/internal/maintainers/{id}", MAINTAINER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.RESOURCE_ID_NOTEXIST.getErrNo()));

        verify(maintainerService, times(1)).findById(MAINTAINER_ID);
    }
}
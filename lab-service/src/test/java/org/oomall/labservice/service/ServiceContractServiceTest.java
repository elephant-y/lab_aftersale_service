package org.oomall.labservice.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oomall.labservice.dao.ServiceContractDao;
import org.oomall.labservice.mapper.Po.ServiceContractPo;
import org.oomall.labservice.model.ReturnNo;
import org.oomall.labservice.model.ReturnObject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class ServiceContractServiceTest {

    @Mock
    private ServiceContractDao serviceContractDao;

    @InjectMocks
    private ServiceContractService serviceContractService;

    private static final Long SHOP_ID = 100L;
    private static final Long REGION_ID = 200L;
    private static final Integer SERVICE_TYPE = 1;
    private static final Long CONTRACT_ID = 300L;

    // 构建一个模拟的 ServiceContractPo 对象
    private ServiceContractPo buildContractPo() {
        ServiceContractPo po = new ServiceContractPo();
        po.setId(CONTRACT_ID);
        po.setShopId(SHOP_ID);
        po.setRegionId(REGION_ID);
        po.setServiceType(SERVICE_TYPE);
        // 根据你的 PO 类实际字段补充其他属性（如 status、gmtCreate 等）
        return po;
    }

    @Test
    @DisplayName("查询可分配服务合同 - 成功找到，返回 OK 和合同对象")
    void findToAssign_shouldReturnOkAndContract_whenFound() {
        // Given: DAO 返回一个有效的合同
        ServiceContractPo po = buildContractPo();
        when(serviceContractDao.findToAssign(eq(SHOP_ID), eq(REGION_ID), eq(SERVICE_TYPE)))
                .thenReturn(po);

        // When
        ReturnObject result = serviceContractService.findToAssign(SHOP_ID, REGION_ID, SERVICE_TYPE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getErrno()).isEqualTo(ReturnNo.OK.getErrNo());
        assertThat(result.getData()).isInstanceOf(ServiceContractPo.class);
        assertThat(result.getData()).isEqualTo(po);

        // 验证 DAO 被调用一次
        verify(serviceContractDao, times(1)).findToAssign(SHOP_ID, REGION_ID, SERVICE_TYPE);

        // log.info 成功日志被触发 → 分支覆盖
    }

    @Test
    @DisplayName("查询可分配服务合同 - 未找到，返回 RESOURCE_ID_NOTEXIST 并记录 error 日志")
    void findToAssign_shouldReturnNotExist_whenContractIsNull() {
        // Given: DAO 返回 null
        when(serviceContractDao.findToAssign(eq(SHOP_ID), eq(REGION_ID), eq(SERVICE_TYPE)))
                .thenReturn(null);

        // When
        ReturnObject result = serviceContractService.findToAssign(SHOP_ID, REGION_ID, SERVICE_TYPE);

        // Then
        assertThat(result.getErrno()).isEqualTo(ReturnNo.RESOURCE_ID_NOTEXIST.getErrNo());
        assertThat(result.getData()).isNull();

        verify(serviceContractDao, times(1)).findToAssign(SHOP_ID, REGION_ID, SERVICE_TYPE);
        // log.error（未查询到）被触发 → 分支覆盖
    }

    @Test
    @DisplayName("查询可分配服务合同 - DAO 抛异常，返回 RESOURCE_ID_NOTEXIST 并记录异常日志")
    void findToAssign_shouldReturnNotExist_whenExceptionThrown() {
        // Given: DAO 抛出异常
        RuntimeException exception = new RuntimeException("数据库错误");
        when(serviceContractDao.findToAssign(eq(SHOP_ID), eq(REGION_ID), eq(SERVICE_TYPE)))
                .thenThrow(exception);

        // When
        ReturnObject result = serviceContractService.findToAssign(SHOP_ID, REGION_ID, SERVICE_TYPE);

        // Then
        assertThat(result.getErrno()).isEqualTo(ReturnNo.RESOURCE_ID_NOTEXIST.getErrNo());
        assertThat(result.getData()).isNull();

        verify(serviceContractDao, times(1)).findToAssign(SHOP_ID, REGION_ID, SERVICE_TYPE);
        // catch 块执行，log.error(..., e) 被触发 → 分支覆盖完成
    }
}

package org.oomall.labaftersale.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oomall.labaftersale.controller.dto.AftersaleApplyDto;
import org.oomall.labaftersale.controller.dto.AftersaleDto;
import org.oomall.labaftersale.dao.AftersaleDao;
import org.oomall.labaftersale.feign.ServiceOrderFeign;
import org.oomall.labaftersale.mapper.po.AftersalePo;
import org.oomall.labaftersale.mapper.po.Consignee;
import org.oomall.labaftersale.model.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AftersaleServiceTest {

    @Mock
    private AftersaleDao aftersaleDao;

    @Mock
    private ServiceOrderFeign serviceOrderFeign;

    @InjectMocks
    private AftersaleService aftersaleService;

    // ================= confirmAftersale =================

    @Test
    void confirmAftersale_success_notRepair() {
        Long shopId = 1L;
        Long aftersaleId = 10L;

        AftersalePo po = new AftersalePo();
        po.setId(aftersaleId);
        po.setShopId(shopId);
        po.setStatus(AftersaleStatus.APPLIED);

        AftersaleDto dto = new AftersaleDto();
        dto.setConfirm(true);
        dto.setType(AftersaleType.RETURN.ordinal());
        dto.setConclusion("同意");

        when(aftersaleDao.findByIdAndShopId(aftersaleId, shopId))
                .thenReturn(po);

        ReturnObject ret =
                aftersaleService.confirmAftersale(shopId, aftersaleId, dto);

        assertEquals(ReturnNo.OK.getErrNo(), ret.getErrno());
        verify(aftersaleDao).save(any(AftersalePo.class));
    }

    @Test
    void confirmAftersale_notExist() {
        when(aftersaleDao.findByIdAndShopId(1L, 1L))
                .thenReturn(null);

        ReturnObject ret =
                aftersaleService.confirmAftersale(1L, 1L, new AftersaleDto());

        assertEquals(ReturnNo.RESOURCE_ID_NOTEXIST.getErrNo(), ret.getErrno());
    }

    @Test
    void confirmAftersale_stateNotAllow() {
        AftersalePo po = new AftersalePo();
        po.setStatus(AftersaleStatus.COMPLETED);

        when(aftersaleDao.findByIdAndShopId(1L, 1L))
                .thenReturn(po);

        ReturnObject ret =
                aftersaleService.confirmAftersale(1L, 1L, new AftersaleDto());

        assertEquals(ReturnNo.STATE_NOT_ALLOW.getErrNo(), ret.getErrno());
    }

    @Test
    void confirmAftersale_success_repair_createServiceOrder() {
        Long shopId = 1L;

        AftersalePo po = new AftersalePo();
        po.setId(1L);
        po.setStatus(AftersaleStatus.APPLIED);
        po.setConsigneeName("张三");
        po.setConsigneeMobile("13800000000");
        po.setConsigneeRegionId(1L);
        po.setConsigneeAddress("地址");

        AftersaleDto dto = new AftersaleDto();
        dto.setConfirm(true);
        dto.setType(AftersaleType.REPAIR.ordinal());
        dto.setServiceType(ServiceOrderType.SITEMENT.ordinal());

        Map<String, Object> data = new HashMap<>();
        data.put("id", 100);

        when(aftersaleDao.findByIdAndShopId(1L, shopId))
                .thenReturn(po);
        when(serviceOrderFeign.createServiceOrder(eq(shopId), eq(1L), any()))
                .thenReturn(new ReturnObject(ReturnNo.OK, data));

        ReturnObject ret =
                aftersaleService.confirmAftersale(shopId, 1L, dto);

        assertEquals(ReturnNo.OK.getErrNo(), ret.getErrno());
        assertNotNull(po.getServiceOrderId());
    }

    // ================= createAftersale =================

    @Test
    void createAftersale_success() {
        AftersaleApplyDto dto = new AftersaleApplyDto();
        dto.setQuantity(1);
        dto.setReason("质量问题");
        dto.setType(AftersaleType.RETURN.ordinal());

        Consignee consignee = new Consignee();
        consignee.setName("李四");
        consignee.setMobile("13900000000");
        dto.setConsignee(consignee);

        when(aftersaleDao.existsByOrderItemId(100L)).thenReturn(false);

        //  模拟保存后返回带ID的对象
        doAnswer(invocation -> {
            AftersalePo po = invocation.getArgument(0);
            po.setId(1L); // 手动赋 ID
            return null; // save 方法返回 void
        }).when(aftersaleDao).save(any(AftersalePo.class));

        ReturnObject ret = aftersaleService.createAftersale(100L, dto);

        assertNotNull(ret.getData());
    }

    @Test
    void createAftersale_duplicate() {
        when(aftersaleDao.existsByOrderItemId(100L))
                .thenReturn(true);

        AftersaleApplyDto dto = new AftersaleApplyDto();
        dto.setQuantity(1);
        dto.setReason("原因");
        dto.setConsignee(new Consignee("张三", "13800138000", 10001L, "北京市朝阳区"));

        ReturnObject ret =
                aftersaleService.createAftersale(100L, dto);

        assertEquals(ReturnNo.STATE_NOT_ALLOW.getErrNo(), ret.getErrno());
    }

    // ================= updateAftersaleStatus =================

    @Test
    void updateAftersaleStatus_success() {
        AftersalePo po = new AftersalePo();
        po.setStatus(AftersaleStatus.PROCESSING);

        when(aftersaleDao.findByIdAndShopId(1L, 1L))
                .thenReturn(po);

        ReturnObject ret =
                aftersaleService.updateAftersaleStatus(
                        1L, 1L, AftersaleStatus.COMPLETED);

        assertEquals(ReturnNo.OK.getErrNo(), ret.getErrno());
        verify(aftersaleDao).save(po);
    }

    @Test
    void updateAftersaleStatus_illegal() {
        AftersalePo po = new AftersalePo();
        po.setStatus(AftersaleStatus.APPLIED);

        when(aftersaleDao.findByIdAndShopId(1L, 1L))
                .thenReturn(po);

        ReturnObject ret =
                aftersaleService.updateAftersaleStatus(
                        1L, 1L, AftersaleStatus.COMPLETED);

        assertEquals(ReturnNo.STATE_NOT_ALLOW.getErrNo(), ret.getErrno());
    }
}

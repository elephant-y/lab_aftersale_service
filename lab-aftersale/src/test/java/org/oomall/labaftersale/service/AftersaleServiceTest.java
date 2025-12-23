package org.oomall.labaftersale.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oomall.labaftersale.controller.dto.AftersaleApplyDto;
import org.oomall.labaftersale.controller.dto.AftersaleDto;
import org.oomall.labaftersale.controller.dto.AftersaleResponseDto;
import org.oomall.labaftersale.dao.AftersaleDao;
import org.oomall.labaftersale.feign.ServiceOrderFeign;
import org.oomall.labaftersale.mapper.po.AftersalePo;
import org.oomall.labaftersale.mapper.po.Consignee;
import org.oomall.labaftersale.model.*;
import org.oomall.labaftersale.service.vo.AftersaleConfirmVo;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.oomall.labaftersale.model.AftersaleType.fromInt;

@ExtendWith(MockitoExtension.class)
class AftersaleServiceTest {

    @Mock
    private AftersaleDao aftersaleDao;

    @Mock
    private ServiceOrderFeign serviceOrderFeign;

    @InjectMocks
    private AftersaleService aftersaleService;

    // ================= findAftersale =================
    @Test
    public void findAftersaleByShopId_success() {
        // Given
        Long shopId = 1L;
        Long id = 1L;

        // 创建测试数据
        AftersalePo mockPo = new AftersalePo();
        mockPo.setId(id);
        mockPo.setShopId(shopId);
        // 设置其他必要字段...

        // Mock DAO返回数据
        when(aftersaleDao.findByIdAndShopId(id, shopId)).thenReturn(mockPo);

        // When
        AftersaleResponseDto result = aftersaleService.findAftersaleByShopId(shopId, id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(shopId, result.getShopId());
        // 验证其他字段...
    }

    @Test
    public void findAftersaleByShopId_aftersaleNotExists() {
        // Given
        Long shopId = 1L;
        Long id = 1L;

        // Mock DAO返回null
        when(aftersaleDao.findByIdAndShopId(id, shopId)).thenReturn(null);

        // When
        AftersaleResponseDto result = aftersaleService.findAftersaleByShopId(shopId, id);

        // Then
        assertNull(result);
        verify(aftersaleDao).findByIdAndShopId(id, shopId);
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
    public void createAftersale_orderItemIdNotExist() {
        // Given
        Long orderItemId = null;
        AftersaleApplyDto applyDto = new AftersaleApplyDto();
        // 设置applyDto的其他必要字段...

        // When
        ReturnObject result = aftersaleService.createAftersale(orderItemId, applyDto);

        // Then
        assertEquals(ReturnNo.FIELD_NOTVALID.getErrNo(), result.getErrno());
        verify(aftersaleDao, never()).save(any());
    }

    @Test
    void createAftersale_quantityNotExist() {
        // Given
        Long orderItemId = 100L;
        AftersaleApplyDto applyDto1 = new AftersaleApplyDto();
        applyDto1.setQuantity(0);
        // 设置applyDto1的其他必要字段...
        // When
        ReturnObject result1 = aftersaleService.createAftersale(orderItemId, applyDto1);

        // Then
        assertEquals(ReturnNo.FIELD_NOTVALID.getErrNo(), result1.getErrno());
        verify(aftersaleDao, never()).save(any());

        AftersaleApplyDto applyDto2 = new AftersaleApplyDto();
        // 设置applyDto2的其他必要字段...
        // When
        ReturnObject result2 = aftersaleService.createAftersale(orderItemId, applyDto2);

        // Then
        assertEquals(ReturnNo.FIELD_NOTVALID.getErrNo(), result2.getErrno());
        verify(aftersaleDao, never()).save(any());
    }

    @Test
    void createAftersale_reasonNotExist() {
        // Given
        Long orderItemId = 100L;
        AftersaleApplyDto applyDto1 = new AftersaleApplyDto();
        applyDto1.setQuantity(1);
        applyDto1.setReason(null);
        applyDto1.setType(AftersaleType.RETURN.ordinal());
        // 设置applyDto的其他必要字段...
        // When
        ReturnObject result1 = aftersaleService.createAftersale(orderItemId, applyDto1);

        // Then
        assertEquals(ReturnNo.FIELD_NOTVALID.getErrNo(), result1.getErrno());
        verify(aftersaleDao, never()).save(any());

        AftersaleApplyDto applyDto2 = new AftersaleApplyDto();
        applyDto2.setQuantity(1);
        applyDto2.setReason(null);
        applyDto2.setType(AftersaleType.RETURN.ordinal());
        // 设置applyDto的其他必要字段...
        // When
        ReturnObject result2 = aftersaleService.createAftersale(orderItemId, applyDto2);

        // Then
        assertEquals(ReturnNo.FIELD_NOTVALID.getErrNo(), result2.getErrno());
        verify(aftersaleDao, never()).save(any());
    }

    @Test
    void createAftersale_consigneeNotExist() {
        // Given
        Long orderItemId = 100L;
        AftersaleApplyDto applyDto1 = new AftersaleApplyDto();
        applyDto1.setQuantity(1);
        applyDto1.setReason("质量问题");
        applyDto1.setType(AftersaleType.RETURN.ordinal());
        // 设置applyDto的其他必要字段...
        // When
        ReturnObject result1 = aftersaleService.createAftersale(orderItemId, applyDto1);

        // Then
        assertEquals(ReturnNo.FIELD_NOTVALID.getErrNo(), result1.getErrno());
        verify(aftersaleDao, never()).save(any());

        AftersaleApplyDto applyDto2 = new AftersaleApplyDto();
        applyDto2.setQuantity(1);
        applyDto2.setReason("质量问题");
        applyDto2.setType(AftersaleType.RETURN.ordinal());
        Consignee consignee = new Consignee();
        consignee.setName("张三");
        applyDto2.setConsignee(consignee);
        // 设置applyDto的其他必要字段...
        // When
        ReturnObject result2 = aftersaleService.createAftersale(orderItemId, applyDto2);

        // Then
        assertEquals(ReturnNo.FIELD_NOTVALID.getErrNo(), result2.getErrno());
        verify(aftersaleDao, never()).save(any());
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

    @Test
    void createAftersale_typeNotAllow() {
        when(aftersaleDao.existsByOrderItemId(100L))
                .thenReturn(false);

        AftersaleApplyDto dto = new AftersaleApplyDto();
        dto.setQuantity(1);
        dto.setReason("质量问题");
        dto.setType(999);

        Consignee consignee = new Consignee();
        consignee.setName("李四");
        consignee.setMobile("13900000000");
        dto.setConsignee(consignee);

        ReturnObject ret = aftersaleService.createAftersale(100L, dto);
        assertEquals(ReturnNo.AFTERSALE_TYPE_ERROR.getErrNo(), ret.getErrno());
    }

    @Test
    void createAftersale_whenException() {
        when(aftersaleDao.existsByOrderItemId(100L))
                .thenReturn(false);

        AftersaleApplyDto dto = new AftersaleApplyDto();
        dto.setQuantity(1);
        dto.setReason("质量问题");
        dto.setType(AftersaleType.RETURN.ordinal());

        Consignee consignee = new Consignee();
        consignee.setName("李四");
        consignee.setMobile("13900000000");
        dto.setConsignee(consignee);

        doThrow(new RuntimeException("数据库异常"))
                .when(aftersaleDao).save(any(AftersalePo.class));

        ReturnObject ret = aftersaleService.createAftersale(100L, dto);
        assertEquals(ReturnNo.SERVICE_ORDER_CREATE_FAIL.getErrNo(), ret.getErrno());
    }

    // ================= confirmAftersale =================
    @Test
    void confirmAftersale_success_refundOnly() {
        Long shopId = 1L;
        Long aftersaleId = 10L;

        AftersalePo po = new AftersalePo();
        po.setId(aftersaleId);
        po.setShopId(shopId);
        po.setStatus(AftersaleStatus.APPLIED);

        AftersaleDto dto = new AftersaleDto();
        dto.setConfirm(true);
        dto.setType(AftersaleType.REFUNDONLY.ordinal());
        dto.setConclusion("同意");

        when(aftersaleDao.findByIdAndShopId(aftersaleId, shopId))
                .thenReturn(po);

        ReturnObject ret =
                aftersaleService.confirmAftersale(shopId, aftersaleId, dto);

        assertEquals(ReturnNo.OK.getErrNo(), ret.getErrno());
        verify(aftersaleDao).save(any(AftersalePo.class));
    }

    @Test
    void confirmAftersale_success_return() {
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

    @Test
    public void confirmAftersale_DtoIsNull() {
        // Given
        Long shopId = 1L;
        Long id = 1L;
        AftersaleDto dto = null;

        // When
        ReturnObject result = aftersaleService.confirmAftersale(shopId, id, dto);

        // Then
        assertEquals(ReturnNo.FIELD_NOTVALID.getErrNo(), result.getErrno());
        verify(aftersaleDao, never()).findByIdAndShopId(anyLong(), anyLong());
        verify(aftersaleDao, never()).save(any());
    }

    @Test
    public void confirmAftersale_InvalidType() {
        // Given
        Long shopId = 1L;
        Long id = 1L;
        AftersaleDto dto1 = new AftersaleDto();
        dto1.setConfirm(true);

        // When
        ReturnObject result1 = aftersaleService.confirmAftersale(shopId, id, dto1);

        // Then
        assertEquals(ReturnNo.AFTERSALE_TYPE_ERROR.getErrNo(), result1.getErrno());
        verify(aftersaleDao, never()).findByIdAndShopId(anyLong(), anyLong());
        verify(aftersaleDao, never()).save(any());

        AftersaleDto dto2 = new AftersaleDto();
        dto2.setConfirm(true);
        dto2.setType(-1); // 无效的售后类型

        // When
        ReturnObject result2 = aftersaleService.confirmAftersale(shopId, id, dto1);

        // Then
        assertEquals(ReturnNo.AFTERSALE_TYPE_ERROR.getErrNo(), result2.getErrno());
        verify(aftersaleDao, never()).findByIdAndShopId(anyLong(), anyLong());
        verify(aftersaleDao, never()).save(any());
    }

    @Test
    public void confirmAftersale_aftersaleNotExists() {
        // 准备测试数据
        Long shopId = 1L;
        Long nonExistentId = 999L;
        AftersaleDto dto = new AftersaleDto();
        dto.setConfirm(true);
        dto.setType(0);

        // Mock DAO返回null（模拟售后单不存在）
        when(aftersaleDao.findByIdAndShopId(nonExistentId, shopId)).thenReturn(null);

        // 执行测试
        ReturnObject result = aftersaleService.confirmAftersale(shopId, nonExistentId, dto);

        // 验证结果
        assertEquals(ReturnNo.RESOURCE_ID_NOTEXIST, result.getCode());
        assertNull(result.getData());

        // 验证DAO方法被调用
        verify(aftersaleDao).findByIdAndShopId(nonExistentId, shopId);
    }


    @Test
    public void confirmAftersale_statusNotApplied() {
        // 准备测试数据
        Long shopId = 1L;
        Long id = 1L;
        AftersaleDto dto = new AftersaleDto();
        dto.setConfirm(true);
        dto.setType(0);

        // 测试处理中状态
        AftersalePo processingPo = new AftersalePo();
        processingPo.setId(id);
        processingPo.setShopId(shopId);
        processingPo.setStatus(AftersaleStatus.PROCESSING);
        when(aftersaleDao.findByIdAndShopId(id, shopId)).thenReturn(processingPo);

        ReturnObject result = aftersaleService.confirmAftersale(shopId, id, dto);
        assertEquals(ReturnNo.STATE_NOT_ALLOW, result.getCode());
    }

    @Test
    public void confirmAftersale_whenRepairTypeWithoutServiceType() {
        // Given
        Long shopId = 1L;
        Long id = 1L;
        AftersaleDto dto = new AftersaleDto();
        dto.setConfirm(true);
        dto.setType(AftersaleType.REPAIR.ordinal());
        dto.setServiceType(null); // 设置为null

        AftersalePo po = new AftersalePo();
        po.setId(id);
        po.setStatus(AftersaleStatus.APPLIED);

        when(aftersaleDao.findByIdAndShopId(id, shopId)).thenReturn(po);

        // When
        ReturnObject result = aftersaleService.confirmAftersale(shopId, id, dto);

        // Then
        assertEquals(ReturnNo.SERVICE_TYPE_ERROR.getErrNo(), result.getErrno());
        verify(aftersaleDao, never()).save(any());
    }

    @Test
    public void confirmAftersale_whenRejected() {
        // Given
        Long shopId = 1L;
        Long id = 1L;
        AftersaleDto dto = new AftersaleDto();
        dto.setConfirm(false); // 设置为拒绝
        dto.setType(AftersaleType.REPAIR.ordinal()); // 设置售后类型

        AftersalePo po = new AftersalePo();
        po.setId(id);
        po.setStatus(AftersaleStatus.APPLIED); // 初始状态为申请中

        // Mock DAO返回数据
        when(aftersaleDao.findByIdAndShopId(id, shopId)).thenReturn(po);

        // When
        ReturnObject result = aftersaleService.confirmAftersale(shopId, id, dto);

        // Then
        assertEquals(ReturnNo.OK.getErrNo(), result.getErrno());
        assertEquals(AftersaleStatus.REJECTED, po.getStatus());
        assertNull(po.getServiceOrderId());
        assertEquals(AftersaleType.REPAIR, po.getType());
        verify(aftersaleDao).save(po);
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
    void updateAftersaleStatus_notExist() {
        when(aftersaleDao.findByIdAndShopId(1L, 1L))
                .thenReturn(null);

        ReturnObject ret =
                aftersaleService.updateAftersaleStatus(
                        1L, 1L, AftersaleStatus.COMPLETED);

        assertEquals(ReturnNo.RESOURCE_ID_NOTEXIST.getErrNo(), ret.getErrno());
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

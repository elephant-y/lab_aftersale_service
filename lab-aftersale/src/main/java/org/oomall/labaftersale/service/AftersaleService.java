package org.oomall.labaftersale.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oomall.labaftersale.controller.dto.AftersaleApplyDto;
import org.oomall.labaftersale.controller.dto.AftersaleDto;
import org.oomall.labaftersale.dao.AftersaleDao;
import org.oomall.labaftersale.feign.ServiceOrderFeign;

import org.oomall.labaftersale.mapper.po.AftersalePo;
import org.oomall.labaftersale.mapper.po.Consignee;
import org.oomall.labaftersale.model.AftersaleStatus;
import org.oomall.labaftersale.model.AftersaleType;
import org.oomall.labaftersale.model.ReturnNo;
import org.oomall.labaftersale.model.ReturnObject;
import org.oomall.labaftersale.service.vo.AftersaleConfirmVo;
import org.oomall.labaftersale.controller.dto.AftersaleResponseDto;
import org.oomall.labaftersale.model.ServiceOrderType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class AftersaleService {

    private final AftersaleDao aftersaleDao;
    private final ServiceOrderFeign serviceOrderFeign;

    @Transactional
    public ReturnObject confirmAftersale(Long shopId, Long id, AftersaleDto dto) {
        // 参数校验
        if (dto == null ) {
            return new ReturnObject(ReturnNo.FIELD_NOTVALID);
        }

        // 查询售后单
        AftersalePo po = aftersaleDao.findByIdAndShopId(id, shopId);
        if (po == null) {
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        }

        // 状态校验
        if (!AftersaleStatus.APPLIED.equals(po.getStatus())) {
            return new ReturnObject(ReturnNo.STATE_NOT_ALLOW);
        }

        if (dto.isConfirm() && AftersaleType.fromInt(dto.getType()) == AftersaleType.REPAIR) {
            if (dto.getServiceType() == null || (ServiceOrderType.fromInt(dto.getServiceType()) != ServiceOrderType.SITEMENT && ServiceOrderType.fromInt(dto.getServiceType()) != ServiceOrderType.SHIPMENT)) {
                return new ReturnObject(ReturnNo.FIELD_NOTVALID, "服务单类型必须为0（上门）或1（寄件）");
            }
        }

        // 处理审核结果
        handleAftersaleReview(po, dto, shopId);
        po.setGmtModified(LocalDateTime.now()); // 更新修改时间

        // 保存更新后的售后单
        aftersaleDao.save(po);

        return new ReturnObject(ReturnNo.OK);
    }

    public AftersaleResponseDto getAftersale(Long shopId, Long id) {
        AftersalePo po = aftersaleDao.findByIdAndShopId(id, shopId);
        if (po == null) {
            return null;
        }
        return convertToResponseDto(po); // 转换Po为DTO
    }

    // 实体转DTO的方法
    private AftersaleResponseDto convertToResponseDto(AftersalePo po) {
        AftersaleResponseDto dto = new AftersaleResponseDto();
        dto.setId(po.getId());
        dto.setCustomerId(po.getCustomerId());
        dto.setOrderItemId(po.getOrderItemId());
        dto.setShopId(po.getShopId());
        dto.setType(po.getType());
        dto.setStatus(po.getStatus());
        dto.setConclusion(po.getConclusion());
        dto.setQuantity(po.getQuantity());
        dto.setServiceOrderId(po.getServiceOrderId());
        dto.setGmtCreate(po.getGmtCreate());
        dto.setGmtModified(po.getGmtModified());

        // 封装收件人信息为Consignee对象
        Consignee consignee = new Consignee();
        consignee.setName(po.getConsigneeName());
        consignee.setMobile(po.getConsigneeMobile());
        consignee.setRegionId(po.getConsigneeRegionId());
        consignee.setAddress(po.getConsigneeAddress());
        dto.setConsignee(consignee);

        return dto;
    }

    /**
     * 顾客提交售后申请，创建售后单（状态=APPLY）
     * @param orderItemId 订单商品ID（PATH参数）
     * @param applyDto 请求体参数
     */
    @Transactional
    public ReturnObject createAftersale(Long orderItemId, AftersaleApplyDto applyDto) {
        // 1. 基础参数校验
        if (orderItemId == null) {
            log.error("【售后模块】创建售后单失败：订单商品ID不能为空");
            return new ReturnObject(ReturnNo.FIELD_NOTVALID, "订单商品ID不能为空");
        }

        // 2. 校验售后数量和原因
        if (applyDto.getQuantity() == null || applyDto.getQuantity() < 1) {
            log.error("【售后模块】创建售后单失败：售后数量不合法，orderItemId={}", orderItemId);
            return new ReturnObject(ReturnNo.FIELD_NOTVALID, "售后数量不能小于1");
        }
        if (applyDto.getReason() == null || applyDto.getReason().trim().isEmpty()) {
            log.error("【售后模块】创建售后单失败：售后原因不能为空，orderItemId={}", orderItemId);
            return new ReturnObject(ReturnNo.FIELD_NOTVALID, "售后原因不能为空");
        }

        // 3. 收件人信息校验（姓名+电话必填）
        Consignee consignee = applyDto.getConsignee();
        if (consignee == null || consignee.getName() == null || consignee.getMobile() == null) {
            log.error("【售后模块】创建售后单失败：收件人姓名/电话不能为空，orderItemId={}", orderItemId);
            return new ReturnObject(ReturnNo.FIELD_NOTVALID, "收件人姓名和电话为必填项");
        }

        // 4. 校验是否已提交过售后（通过order_item_id唯一约束）
        if (aftersaleDao.existsByOrderItemId(orderItemId)) {
            log.error("【售后模块】创建售后单失败：该订单商品已提交售后，orderItemId={}", orderItemId);
            return new ReturnObject(ReturnNo.STATE_NOT_ALLOW, "该订单商品已提交售后，请勿重复申请");
        }

        AftersaleType aftersaleType = AftersaleType.fromInt(applyDto.getType());
        if (aftersaleType == null) {
            log.error("【售后模块】创建售后单失败：售后类型不合法，orderItemId={}", orderItemId);
            return new ReturnObject(ReturnNo.AFTERSALE_TYPE_ERROR);
        }

        // 5. 构造售后单Po（状态=APPLIED）
        AftersalePo aftersalePo = new AftersalePo();
        aftersalePo.setOrderItemId(orderItemId); // 关联订单商品ID
        aftersalePo.setCustomerId(1L);
        aftersalePo.setShopId(1L); // 若有店铺ID来源（如从订单商品查询）可替换，暂无则默认1
        aftersalePo.setStatus(AftersaleStatus.APPLIED); // 初始状态：申请中
        aftersalePo.setType(AftersaleType.fromInt(applyDto.getType())); // 转换为AftersaleType枚举
        aftersalePo.setQuantity(applyDto.getQuantity()); // 售后数量
        aftersalePo.setConclusion(applyDto.getReason() == null ? "" : applyDto.getReason()); // 售后原因存入conclusion字段
        // 填充收件人信息
        aftersalePo.setConsigneeName(consignee.getName());
        aftersalePo.setConsigneeMobile(consignee.getMobile());
        aftersalePo.setConsigneeRegionId(consignee.getRegionId());
        aftersalePo.setConsigneeAddress(consignee.getAddress());
        aftersalePo.setServiceOrderId(null); // 未创建服务单，默认为null

        aftersalePo.setGmtCreate(LocalDateTime.now()); // 设置创建时间
        aftersalePo.setGmtModified(LocalDateTime.now()); // 设置修改时间
        // 6. 入库保存
        try {
            aftersaleDao.save(aftersalePo);
            log.info("【售后模块】售后单创建成功，orderItemId={}, 售后单ID={}, 状态=APPLY",
                    orderItemId, aftersalePo.getId());
        } catch (Exception e) {
            log.error("【售后模块】创建售后单入库失败，orderItemId={}", orderItemId, e);
            return new ReturnObject(ReturnNo.SERVICE_ORDER_CREATE_FAIL, "售后单创建失败");
        }

        // 7. 返回结果（包含售后单ID）
        return new ReturnObject(aftersalePo.getId());
    }

    /**
     * 处理售后单审核结果
     */
    private void handleAftersaleReview(AftersalePo po, AftersaleDto dto, Long shopId) {
        // 设置审核结论
        po.setConclusion(dto.getConclusion());
        AftersaleConfirmVo vo = new AftersaleConfirmVo();
        vo.setType(dto.getType());
        Consignee consignee = new Consignee();
        consignee.setName(po.getConsigneeName());
        consignee.setMobile(po.getConsigneeMobile());
        consignee.setRegionId(po.getConsigneeRegionId());
        consignee.setAddress(po.getConsigneeAddress());
        vo.setConsignee(consignee);
        vo.setServiceType(dto.getServiceType());

        // 审核拒绝
        if (!dto.isConfirm()) {
            handleRejectReview(po, dto, shopId);
            return;
        }

        // 审核通过
        handleApproveReview(po, vo, shopId);
    }

    /**
     * 处理审核拒绝
     */
    private void handleRejectReview(AftersalePo po, AftersaleDto dto, Long shopId) {
        po.setStatus(AftersaleStatus.REJECTED);
        po.setServiceOrderId(null);
        po.setType(AftersaleType.fromInt(dto.getType()));
        log.info("【售后模块】审核拒绝售后单id={}", po.getId());
    }

    /**
     * 处理审核通过
     */
    private void handleApproveReview(AftersalePo po, AftersaleConfirmVo vo, Long shopId) {
        po.setStatus(AftersaleStatus.PROCESSING);
        // 设置售后类型
        po.setType(AftersaleType.fromInt(vo.getType()));

        // 创建服务单（仅维修类型）
        if (AftersaleType.fromInt(vo.getType()) == AftersaleType.REPAIR) { // 2代表维修类型
            ReturnObject ret = serviceOrderFeign.createServiceOrder(shopId, po.getId(), vo);

            if (ret.getErrno() != ReturnNo.OK.getErrNo()) {
                log.error("【售后模块】服务单创建失败，errno={}, 售后单id={}", ret.getErrno(), po.getId());
                throw new RuntimeException(ReturnNo.SERVICE_ORDER_CREATE_FAIL.getMessage());
            }

            if (ret.getData() != null) {
                Map<String, Object> data = (Map<String, Object>) ret.getData();
                Integer id= (Integer) data.get("id");
                po.setServiceOrderId(Long.valueOf(id));
                log.info("【售后模块】审核通过售后单id={}, 创建服务单成功，服务单id={}", po.getId(), po.getServiceOrderId());
            }
        }
    }

    /**
     * 更新售后单状态
     */
    public ReturnObject updateAftersaleStatus(Long shopId, Long id, AftersaleStatus status) {
        // 1. 查询售后单是否存在且属于当前店铺
        AftersalePo po = aftersaleDao.findByIdAndShopId(id, shopId);
        if (po == null) {
            log.error("【售后模块】更新状态失败：售后单不存在或不属于当前商户，shopId={}, aftersaleId={}", shopId, id);
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST, "售后单不存在或不属于当前商户");
        }

        // 2. 校验状态转换是否合法
        if (!isStatusTransitionValid(po.getStatus(), status)) {
            log.error("【售后模块】更新状态失败：状态转换不合法，当前状态={}, 目标状态={}, aftersaleId={}",
                    po.getStatus(), status, id);
            return new ReturnObject(ReturnNo.STATE_NOT_ALLOW, "当前状态不允许转换为目标状态");
        }

        // 3. 更新状态和修改时间
        po.setStatus(status);
        po.setGmtModified(LocalDateTime.now());
        aftersaleDao.save(po);

        log.info("【售后模块】售后单状态更新成功，aftersaleId={}, 新状态={}", id, status);
        return new ReturnObject(ReturnNo.OK);
    }

    /**
     * 校验状态转换是否合法
     */
    private boolean isStatusTransitionValid(AftersaleStatus currentStatus, AftersaleStatus targetStatus) {
        // 只有处理中（PROCESSING）可以转为已完成（COMPLETED）
        if (currentStatus == AftersaleStatus.PROCESSING && targetStatus == AftersaleStatus.COMPLETED) {
            return true;
        }
        // 其他合法转换规则可在此添加
        return false;
    }

}
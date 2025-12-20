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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (!AftersaleStatus.APPLY.equals(po.getStatus())) {
            return new ReturnObject(ReturnNo.STATE_NOT_ALLOW);
        }

        if (dto.isConfirm() && selectType(dto.getType()) == AftersaleType.REPAIR) {
            if (dto.getServiceType() == null || (dto.getServiceType() != 0 && dto.getServiceType() != 1)) {
                return new ReturnObject(ReturnNo.FIELD_NOTVALID, "服务单类型必须为0（上门）或1（寄件）");
            }
        }

        // 处理审核结果
        handleAftersaleReview(po, dto, shopId);

        // 保存更新后的售后单
        aftersaleDao.save(po);

        return new ReturnObject(ReturnNo.OK);
    }

    public AftersalePo getAftersale(Long shopId, Long id) {
        AftersalePo po = aftersaleDao.findByIdAndShopId(id, shopId);
        return po;
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

        // 5. 构造售后单Po（状态=APPLY）
        AftersalePo aftersalePo = new AftersalePo();
        aftersalePo.setOrderItemId(orderItemId); // 关联订单商品ID
        aftersalePo.setCustomerId(1L);
        aftersalePo.setShopId(1L); // 若有店铺ID来源（如从订单商品查询）可替换，暂无则默认1
        aftersalePo.setStatus(AftersaleStatus.APPLY); // 初始状态：申请中
        aftersalePo.setType(selectType(applyDto.getType())); // 转换为AftersaleType枚举
        aftersalePo.setQuantity(applyDto.getQuantity()); // 售后数量
        aftersalePo.setConclusion(applyDto.getReason() == null ? "" : applyDto.getReason()); // 售后原因存入conclusion字段
        // 填充收件人信息
        aftersalePo.setConsigneeName(consignee.getName());
        aftersalePo.setConsigneeMobile(consignee.getMobile());
        aftersalePo.setConsigneeRegionId(consignee.getRegionId());
        aftersalePo.setConsigneeAddress(consignee.getAddress());
        aftersalePo.setServiceOrderId(null); // 未创建服务单，默认为null

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
        po.setStatus(AftersaleStatus.REJECT);
        po.setServiceOrderId(null);
        po.setType(selectType(dto.getType()));
        log.info("【售后模块】审核拒绝售后单id={}", po.getId());
    }

    /**
     * 处理审核通过
     */
    private void handleApproveReview(AftersalePo po, AftersaleConfirmVo vo, Long shopId) {
        po.setStatus(AftersaleStatus.CONFIRMED);
        // 设置售后类型
        po.setType(selectType(vo.getType()));

        // 创建服务单（仅维修类型）
        if (selectType(vo.getType()) == AftersaleType.REPAIR) { // 2代表维修类型
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

    private AftersaleType selectType(Integer type) {
        if (type == 0) {return AftersaleType.RETURN;}
        else if (type == 1) {return AftersaleType.EXCHANGE;}
        else {return AftersaleType.REPAIR;}
    }
}
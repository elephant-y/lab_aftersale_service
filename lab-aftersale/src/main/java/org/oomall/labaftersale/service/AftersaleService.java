package org.oomall.labaftersale.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        if (vo.getType() == 2) { // 2代表维修类型
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
        else if (type == 2) {return AftersaleType.EXCHANGE;}
        else {return AftersaleType.REPAIR;}
    }
}
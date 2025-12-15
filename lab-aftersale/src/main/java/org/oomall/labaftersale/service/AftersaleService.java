package org.oomall.labaftersale.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oomall.labaftersale.dao.AftersaleDao;
import org.oomall.labaftersale.feign.ServiceOrderFeign;
import org.oomall.labaftersale.mapper.po.AftersalePo;
import org.oomall.labaftersale.model.AftersaleStatus;
import org.oomall.labaftersale.model.AftersaleType;
import org.oomall.labaftersale.model.ReturnNo;
import org.oomall.labaftersale.model.ReturnObject;
import org.oomall.labaftersale.service.vo.AftersaleConfirmVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AftersaleService {


    private final AftersaleDao aftersaleDao;

    private final ServiceOrderFeign serviceOrderFeign;

    @Transactional
    public ReturnObject confirmAftersale(Long shopId, Long id, AftersaleConfirmVo vo) {

        if (vo == null || vo.getConfirm() == null) {
            return new ReturnObject(ReturnNo.FIELD_NOTVALID);
        }

        AftersalePo po = aftersaleDao.findByIdAndShopId(id, shopId);
        if (po == null) {
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        }

        if (!AftersaleStatus.APPLY.equals(po.getStatus())) {
            return new ReturnObject(ReturnNo.STATE_NOT_ALLOW);
        }

        if (!AftersaleType.REPAIR.equals(po.getType())) {
            return new ReturnObject(ReturnNo.AFTERSALE_TYPE_ERROR);
        }

        if (!vo.getConfirm()) {
            po.setStatus(AftersaleStatus.REJECT);
            aftersaleDao.save(po);
            log.info("【售后模块】审核拒绝售后单id={}", id);
            return new ReturnObject(ReturnNo.OK);
        }

        po.setStatus(AftersaleStatus.CONFIRMED);
        aftersaleDao.save(po);
        log.info("【售后模块】审核通过售后单id={}, 调用服务模块创建服务单", id);

        ReturnObject ret = serviceOrderFeign.createServiceOrder(shopId, id);
        if (ret.getErrno() != ReturnNo.OK.getErrNo()) {
            log.error("【售后模块】服务单创建失败，errno={}", ret.getErrno());
            return new ReturnObject(ReturnNo.SERVICE_ORDER_CREATE_FAIL);
        }

        return new ReturnObject(ReturnNo.OK);
    }
}

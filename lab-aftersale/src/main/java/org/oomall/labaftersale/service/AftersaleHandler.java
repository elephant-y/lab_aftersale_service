package org.oomall.labaftersale.service;

import org.oomall.labaftersale.feign.ServiceOrderFeign;
import org.oomall.labaftersale.mapper.po.AftersalePo;
import org.oomall.labaftersale.model.AftersaleStatus;
import org.oomall.labaftersale.model.ReturnNo;
import org.oomall.labaftersale.model.ReturnObject;
import org.oomall.labaftersale.service.vo.AftersaleConfirmVo;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

// 抽象售后处理类（父类）
public abstract class AftersaleHandler {
    protected Long shopId;
    protected AftersalePo aftersalePo;
    protected AftersaleConfirmVo confirmVo;

    public AftersaleHandler(Long shopId, AftersalePo aftersalePo, AftersaleConfirmVo confirmVo) {
        this.shopId = shopId;
        this.aftersalePo = aftersalePo;
        this.confirmVo = confirmVo;
    }

    /**
     * 模板方法：公共流程
     */
    public final void execute() {
        // 公共逻辑：设置状态为处理中
        aftersalePo.setStatus(AftersaleStatus.PROCESSING);
        // 执行子类具体逻辑
        doExecute();
    }

    /**
     * 子类具体实现
     */
    protected abstract void doExecute();
}

// 退款处理子类
@Slf4j
class RefundOnlyHandler extends AftersaleHandler {

    public RefundOnlyHandler(Long shopId, AftersalePo aftersalePo, AftersaleConfirmVo confirmVo) {
        super(shopId, aftersalePo, confirmVo);
    }

    @Override
    protected void doExecute() {
        // 仅退款逻辑，暂为空
    }
}

// 退货处理子类
@Slf4j
class ReturnHandler extends AftersaleHandler {

    public ReturnHandler(Long shopId, AftersalePo aftersalePo, AftersaleConfirmVo confirmVo) {
        super(shopId, aftersalePo, confirmVo);
    }

    @Override
    protected void doExecute() {
        // 退货逻辑，暂为空
    }
}

// 维修处理子类
@Slf4j
class RepairHandler extends AftersaleHandler {

    private final ServiceOrderFeign serviceOrderFeign;

    public RepairHandler(Long shopId, AftersalePo aftersalePo, AftersaleConfirmVo confirmVo,
                         ServiceOrderFeign serviceOrderFeign) {
        super(shopId, aftersalePo, confirmVo);
        this.serviceOrderFeign = serviceOrderFeign;
    }

    @Override
    protected void doExecute() {
        // 保留原创建服务单代码
        ReturnObject ret = serviceOrderFeign.createServiceOrder(shopId, aftersalePo.getId(), confirmVo);

        if (ret.getErrno() != ReturnNo.OK.getErrNo()) {
            log.error("【售后模块】服务单创建失败，errno={}, 售后单id={}", ret.getErrno(), aftersalePo.getId());
            throw new RuntimeException(ReturnNo.SERVICE_ORDER_CREATE_FAIL.getMessage());
        }

        if (ret.getData() != null) {
            Map<String, Object> data = (Map<String, Object>) ret.getData();
            Integer id = (Integer) data.get("id");
            aftersalePo.setServiceOrderId(Long.valueOf(id));
            log.info("【售后模块】审核通过售后单id={}, 创建服务单成功，服务单id={}",
                    aftersalePo.getId(), aftersalePo.getServiceOrderId());
        }
    }
}
package org.oomall.labservice.service.servicetype;

import lombok.extern.slf4j.Slf4j;
import org.oomall.labservice.mapper.Po.ServiceOrderPo;
import org.oomall.labservice.model.ReturnNo;
import org.oomall.labservice.model.ServiceOrderStatus;
@Slf4j
public abstract class ServiceTypeHandler {

    /* ===== 接单 ===== */
    public void accept(ServiceOrderPo order) {
        checkAcceptState(order);
        order.setStatus(ServiceOrderStatus.ACCEPTED.getCode());
        afterAccept(order);
    }

    /* ===== 拒单（默认通用） ===== */
    public void refuse(ServiceOrderPo order) {
        order.setStatus(ServiceOrderStatus.REJECTED.getCode());
    }

    /* ===== 完成 ===== */
    public void finish(ServiceOrderPo order) {
        checkFinishState(order);
        order.setStatus(ServiceOrderStatus.FINISHED.getCode());
    }

    /* ===== 钩子方法 ===== */
    protected void afterAccept(ServiceOrderPo order) {}

    protected abstract void checkAcceptState(ServiceOrderPo order);

    protected abstract void checkFinishState(ServiceOrderPo order);

    /* ===== 寄件专用（默认不支持） ===== */
    public void receiveProduct(ServiceOrderPo order) {
        log.info("【寄件服务】该状态不支持接收产品，serviceOrderId={}", order.getId());
    }

    public void sendProduct(ServiceOrderPo order) {
        log.info("【寄件服务】该状态不支持发送产品，serviceOrderId={}", order.getId());
    }
}

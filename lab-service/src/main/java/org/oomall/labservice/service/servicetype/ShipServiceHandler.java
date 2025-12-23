package org.oomall.labservice.service.servicetype;

import lombok.extern.slf4j.Slf4j;
import org.oomall.labservice.mapper.Po.ServiceOrderPo;
import org.oomall.labservice.model.ReturnNo;
import org.oomall.labservice.model.ServiceOrderStatus;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShipServiceHandler extends ServiceTypeHandler {

    @Override
    protected void checkAcceptState(ServiceOrderPo order) {
        if (!ServiceOrderStatus.ASSIGNED.same(order)) {
            log.info("【寄件服务】接单失败，serviceOrderId={}，状态不是已派单", order.getId());
        }
    }

    @Override
    protected void checkFinishState(ServiceOrderPo order) {
        if (!ServiceOrderStatus.SENT.same(order)) {
            log.info("【寄件服务】完成失败，serviceOrderId={}，状态不是已发出", order.getId());
        }
    }

    @Override
    public void receiveProduct(ServiceOrderPo order) {
        if (!ServiceOrderStatus.ACCEPTED.same(order)) {
            log.info("【寄件服务】签收失败，serviceOrderId={}，状态不是已接单", order.getId());
        }
        order.setStatus(ServiceOrderStatus.RECEIVED.getCode());
        log.info("【寄件服务】产品已签收，serviceOrderId={}", order.getId());
    }

    @Override
    public void sendProduct(ServiceOrderPo order) {
        if (!ServiceOrderStatus.RECEIVED.same(order)) {
            log.info("【寄件服务】发出失败，serviceOrderId={}，状态不是已签收", order.getId());
        }
        order.setStatus(ServiceOrderStatus.SENT.getCode());
        log.info("【寄件服务】产品已发出，serviceOrderId={}", order.getId());
    }
}
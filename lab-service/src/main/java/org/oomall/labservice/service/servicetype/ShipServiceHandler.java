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
            throw new IllegalStateException("寄件服务接单要求状态为已派单");
        }
    }

    @Override
    protected void checkFinishState(ServiceOrderPo order) {
        if (!ServiceOrderStatus.SENT.same(order)) {
            throw new IllegalStateException("寄件服务完成要求状态为已发出");
        }
    }

    @Override
    public void receiveProduct(ServiceOrderPo order) {
        if (!ServiceOrderStatus.ACCEPTED.same(order)) {
            throw new IllegalStateException("寄件服务签收要求状态为已接单");
        }
        order.setStatus(ServiceOrderStatus.RECEIVED.getCode());
        log.info("【寄件服务】产品已签收，serviceOrderId={}", order.getId());
    }

    @Override
    public void sendProduct(ServiceOrderPo order) {
        if (!ServiceOrderStatus.RECEIVED.same(order)) {
            throw new IllegalStateException("寄件服务发出要求状态为已签收");
        }
        order.setStatus(ServiceOrderStatus.SENT.getCode());
        log.info("【寄件服务】产品已发出，serviceOrderId={}", order.getId());
    }
}
package org.oomall.labservice.service.servicetype;

import lombok.extern.slf4j.Slf4j;
import org.oomall.labservice.mapper.Po.ServiceOrderPo;
import org.oomall.labservice.model.ReturnNo;
import org.oomall.labservice.model.ServiceOrderStatus;
import org.springframework.stereotype.Component;



@Component
@Slf4j
public class SiteServiceHandler extends ServiceTypeHandler {

    @Override
    protected void checkAcceptState(ServiceOrderPo order) {
        if (!ServiceOrderStatus.ASSIGNED.same(order)) {
            log.info("【上门服务】接单失败，serviceOrderId={}，状态不是已派单", order.getId());
        }
    }

    @Override
    protected void checkFinishState(ServiceOrderPo order) {
        if (!ServiceOrderStatus.ACCEPTED.same(order)) {
            log.info("【上门服务】完成失败，serviceOrderId={}，状态不是已接单", order.getId());
        }
    }

    @Override
    protected void afterAccept(ServiceOrderPo order) {
        log.info("【上门服务】接单成功，serviceOrderId={}", order.getId());
    }
}
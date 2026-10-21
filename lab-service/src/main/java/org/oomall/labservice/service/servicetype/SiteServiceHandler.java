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
            // 状态机执法:非法流转直接抛异常,由 Service 层转为业务错误码
            throw new IllegalStateException("上门服务接单要求状态为已派单");
        }
    }

    @Override
    protected void checkFinishState(ServiceOrderPo order) {
        if (!ServiceOrderStatus.ACCEPTED.same(order)) {
            throw new IllegalStateException("上门服务完成要求状态为已接单");
        }
    }

    @Override
    protected void afterAccept(ServiceOrderPo order) {
        log.info("【上门服务】接单成功，serviceOrderId={}", order.getId());
    }
}
package org.oomall.labservice.model;

import com.fasterxml.jackson.annotation.JsonValue;
import org.oomall.labservice.mapper.Po.ServiceOrderPo;

public enum ServiceOrderStatus {
    WAITING(0),  // 待派单
    ASSIGNED(1), // 已派单
    CANCELLED(2),// 已取消
    ACCEPTED(3), // 已接单
    REJECTED(4), // 已拒单
    RECEIVED(5), // 已签收
    SENT(6),     // 已发货
    FINISHED(7); // 已完成
    private final int  code;
    public int getCode() {
        return code;
    }
    ServiceOrderStatus(int code) {
        this.code = code;
    }
    @JsonValue
    public String getStatusName() {
        return this.name();
    }
        public boolean same(ServiceOrderPo order) {
            return this.code == order.getStatus();
        }

}

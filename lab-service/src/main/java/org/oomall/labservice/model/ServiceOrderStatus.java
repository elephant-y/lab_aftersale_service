package org.oomall.labservice.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ServiceOrderStatus {
    WAITING,  // 待派单
    ASSIGNED, // 已派单
    CANCELLED,// 已取消
    ACCEPTED, // 已接单
    REJECTED, // 已拒单
    RECEIVED, // 已签收
    SENT,     // 已发货
    FINISHED; // 已完成

    @JsonValue
    public String getStatusName() {
        return this.name();
    }
}

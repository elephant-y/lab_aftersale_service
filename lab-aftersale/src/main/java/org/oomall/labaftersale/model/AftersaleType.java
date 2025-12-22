package org.oomall.labaftersale.model;


public enum AftersaleType {
    REFUNDONLY, // 仅退款
    RETURN,     // 退货退款
    REPAIR;     // 维修

    public static AftersaleType fromInt(Integer type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case 0: return REFUNDONLY;
            case 1: return RETURN;
            case 2: return REPAIR;
            default: return null;
        }
    }
}

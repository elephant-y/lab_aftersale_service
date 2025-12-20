package org.oomall.labaftersale.model;


public enum AftersaleType {
    RETURN,   // 退货
    EXCHANGE, // 换货
    REPAIR;    // 维修

    public static AftersaleType fromInt(Integer type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case 0: return RETURN;
            case 1: return EXCHANGE;
            case 2: return REPAIR;
            default: return null;
        }
    }
}

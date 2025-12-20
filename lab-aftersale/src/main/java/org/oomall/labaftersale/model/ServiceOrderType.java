package org.oomall.labaftersale.model;

public enum ServiceOrderType {
    SITEMENT,   // 上门
    SHIPMENT;    // 寄件

    public static ServiceOrderType fromInt(Integer type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case 0: return SITEMENT;
            case 1: return SHIPMENT;
            default: return null;
        }
    }
}

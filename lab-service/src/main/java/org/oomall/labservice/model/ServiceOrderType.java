package org.oomall.labservice.model;

public enum ServiceOrderType {
    SITEMENT(0),   // 上门
    SHIPMENT(1);   // 寄件

    private final int code;

    ServiceOrderType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
        public static ServiceOrderType from(int code) {
            for (ServiceOrderType type : values()) {
                if (type.getCode() == code) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No enum constant with code " + code);
        }
}

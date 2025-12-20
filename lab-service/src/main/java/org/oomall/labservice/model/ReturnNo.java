package org.oomall.labservice.model;

public enum ReturnNo {
    OK(0, "成功"),

    // ===== 通用 =====
    FIELD_NOTVALID(503, "字段不合法"),
    RESOURCE_ID_NOTEXIST(504, "资源不存在"),
    STATE_NOT_ALLOW(507, "当前状态不允许此操作"),

    // ===== 售后 / 服务单相关 =====
    AFTERSALE_TYPE_ERROR(801, "售后/服务类型错误"),
    SERVICE_ORDER_CREATE_FAIL(802, "创建服务单失败");

    private final int errNo;
    private final String message;

    ReturnNo(int errNo, String message) {
        this.errNo = errNo;
        this.message = message;
    }

    public int getErrNo() {
        return errNo;
    }

    public String getMessage() {
        return message;
    }
}

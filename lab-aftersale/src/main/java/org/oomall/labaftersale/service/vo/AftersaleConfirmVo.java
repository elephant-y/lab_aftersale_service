package org.oomall.labaftersale.service.vo;

import lombok.Data;

@Data
public class AftersaleConfirmVo {
    private Boolean confirm;
    private String conclusion;
    private Integer type; // 0换货，1退货, 2维修
}

package org.oomall.labaftersale.service.vo;

import lombok.Data;

import org.oomall.labaftersale.mapper.po.Consignee;

@Data
public class AftersaleConfirmVo {
    private Integer type;
    private Consignee consignee;
}

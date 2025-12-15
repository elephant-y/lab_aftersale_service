package org.oomall.labservice.mapper.Po;

import lombok.Data;

@Data
public class ServiceOrderPo {
    private Long id;
    private Long shopId;
    private Long aftersaleId;
    private Integer type;
    private String consignee;
}

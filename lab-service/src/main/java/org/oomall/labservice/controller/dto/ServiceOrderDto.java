package org.oomall.labservice.controller.dto;

import lombok.Data;
import org.oomall.labservice.mapper.Po.Consignee;

@Data
public class ServiceOrderDto {
    private Integer type;
    private Consignee consignee;
}

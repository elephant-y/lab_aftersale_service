package org.oomall.labservice.feign.dto;

import lombok.Data;


import org.oomall.labservice.feign.po.AftersalePo;
import org.oomall.labservice.model.AftersaleStatus;
import org.oomall.labservice.model.AftersaleType;

@Data
public class AftersaleDto {
    private Long id;
    private AftersaleType type;
    private AftersaleStatus status;

    public AftersaleDto(AftersalePo po) {
        this.id = po.getId();
        this.type = po.getType();
        this.status = po.getStatus();
    }
}

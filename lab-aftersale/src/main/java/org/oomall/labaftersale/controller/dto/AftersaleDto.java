package org.oomall.labaftersale.controller.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.oomall.labaftersale.mapper.po.AftersalePo;
import org.oomall.labaftersale.model.AftersaleStatus;
import org.oomall.labaftersale.model.AftersaleType;

@Data
@AllArgsConstructor
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

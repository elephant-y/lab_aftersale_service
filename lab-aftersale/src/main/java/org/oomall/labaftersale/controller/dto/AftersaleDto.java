package org.oomall.labaftersale.controller.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.oomall.labaftersale.mapper.po.AftersalePo;
import org.oomall.labaftersale.model.AftersaleStatus;
import org.oomall.labaftersale.model.AftersaleType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AftersaleDto {
    private boolean confirm;
    private String conclusion;
    private Integer type;
}

package org.oomall.labaftersale.mapper.po;


import lombok.Data;
import org.oomall.labaftersale.model.AftersaleStatus;
import org.oomall.labaftersale.model.AftersaleType;

@Data
public class AftersalePo {
    private Long id;
    private Long shopId;
    private AftersaleType type;
    private AftersaleStatus status;
}

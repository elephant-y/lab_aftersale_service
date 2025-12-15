package org.oomall.labservice.feign.po;

import lombok.Data;
import org.oomall.labservice.model.AftersaleStatus;
import org.oomall.labservice.model.AftersaleType;


@Data
public class AftersalePo {
    private Long id;
    private Long shopId;
    private AftersaleType type;
    private AftersaleStatus status;
}

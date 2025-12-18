package org.oomall.labservice.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


import lombok.NoArgsConstructor;
import org.oomall.labservice.feign.po.AftersalePo;
import org.oomall.labservice.model.AftersaleStatus;
import org.oomall.labservice.model.AftersaleType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AftersaleDto {
    private Long id;
    private AftersaleType type;
    private AftersaleStatus status;


}

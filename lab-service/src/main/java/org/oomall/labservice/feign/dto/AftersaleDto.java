package org.oomall.labservice.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


import lombok.NoArgsConstructor;
import org.oomall.labservice.model.AftersaleType;
import org.oomall.labservice.model.AftersaleStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AftersaleDto {
    private Long id;
    private AftersaleType type;
    private AftersaleStatus status;

}

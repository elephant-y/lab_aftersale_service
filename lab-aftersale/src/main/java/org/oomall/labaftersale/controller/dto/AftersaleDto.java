package org.oomall.labaftersale.controller.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AftersaleDto {
    private boolean confirm;
    private String conclusion;
    private Integer type;
    private Integer serviceType;
}

package org.oomall.labservice.controller.dto;

import lombok.Data;

@Data
public class Consignee {
    private String name;
    private String mobile;
    private Integer regionId;
    private String address;
}

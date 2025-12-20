package org.oomall.labservice.service.vo;

import lombok.Data;

@Data
public class ServiceOrderVo {
        private Long id;
        private Integer serviceType;
        private Integer consigneeRegionId;
}

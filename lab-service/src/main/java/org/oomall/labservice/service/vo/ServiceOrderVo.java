package org.oomall.labservice.service.vo;

import lombok.Data;
import org.oomall.labservice.mapper.Po.Consignee;

@Data
public class ServiceOrderVo {
        private Long id;
        private Integer serviceType;
        private Consignee consignee;
}

package org.oomall.labservice.service.vo;

import lombok.Data;
import org.oomall.labservice.model.ServiceOrderStatus;

import java.time.LocalDateTime;

@Data
public class ServiceOrderDetailVo {
    private Long id;
    private Long shopId;
    private Long aftersaleId;
    private Integer serviceType;
    private ServiceOrderStatus status; // 枚举类型，直接返回名称
    private ConsigneeVo consignee;
    private MaintainerVo maintainer;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;

    @Data
    public static class ConsigneeVo {
        private String consigneeName;
        private String consigneeMobile;
        private Integer consigneeRegionId;
        private String consigneeAddress;
    }

    @Data
    public static class MaintainerVo {
        private Long id;
        private String maintainerName;
        private String maintainerMobile;
        private String maintainerAddress;
    }
}
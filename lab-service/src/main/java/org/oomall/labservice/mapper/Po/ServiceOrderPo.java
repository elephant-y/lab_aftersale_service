package org.oomall.labservice.mapper.Po;



import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "service_order")
public class ServiceOrderPo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "aftersale_id")
    private Long aftersaleId;

    @Column(name = "service_type")
    private Integer serviceType;

    @Column(name = "consignee_name")
    private String consigneeName;
    @Column(name = "consignee_mobile")
    private String consigneeMobile;
    @Column(name = "consignee_region_id")
    private Integer consigneeRegionId;
    @Column(name = "consignee_address")
    private String consigneeAddress;

    @Column(name = "status")
    private Integer status=0;

    @Column(name = "maintainer_id")
    private Long maintainerId;
    @Column(name = "maintainer_name")
    private String maintainerName;
    @Column(name = "maintainer_mobile")
    private String maintainerMobile;
    @Column(name = "maintainer_address")
    private String maintainerAddress;

    @Column(name = "gmt_create")
    private LocalDateTime gmtCreate;
    @Column(name = "gmt_modified")
    private LocalDateTime gmtModified;
}

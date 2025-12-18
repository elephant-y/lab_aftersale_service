package org.oomall.labaftersale.mapper.po;


import jakarta.persistence.*;
import lombok.Data;
import org.oomall.labaftersale.model.AftersaleStatus;
import org.oomall.labaftersale.model.AftersaleType;

@Data
@Entity
@Table(name = "aftersale")
public class AftersalePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id")
    private Long shopId;

    @Enumerated(EnumType.ORDINAL)
    private AftersaleType type;

    @Enumerated(EnumType.ORDINAL)
    private AftersaleStatus status;

    @Column(name = "conclusion")
    private String conclusion;

    @Column(name = "service_order_id")
    private Long serviceOrderId;
    @Column(name = "consignee_name")
    private String consigneeName;
    @Column(name = "consignee_mobile")
    private String consigneeMobile;
    @Column(name = "consignee_region_id")
    private Long consigneeRegionId;
    @Column(name = "consignee_address")
    private String consigneeAddress;
}

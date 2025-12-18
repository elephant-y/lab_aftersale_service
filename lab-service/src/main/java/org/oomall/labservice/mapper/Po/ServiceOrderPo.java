package org.oomall.labservice.mapper.Po;



import jakarta.persistence.*;
import lombok.Data;


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

    // 如果你希望保留整数类型，可直接用 Integer
    @Column(name = "type")
    private Integer type;

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
}

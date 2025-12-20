package org.oomall.labaftersale.mapper.po;

import jakarta.persistence.*;
import lombok.Data;
import org.oomall.labaftersale.model.AftersaleStatus;
import org.oomall.labaftersale.model.AftersaleType;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "aftersale")
public class AftersalePo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false) // 与表字段名一致，非空
    private Long customerId;

    // 新增：关联订单商品ID（PATH参数order_item_id）
    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @Column(name = "shop_id")
    private Long shopId;

    @Enumerated(EnumType.ORDINAL)
    private AftersaleType type;

    @Enumerated(EnumType.ORDINAL)
    private AftersaleStatus status;

    @Column(name = "conclusion")
    private String conclusion;

    @Column(name = "quantity") // 新增：存储售后数量
    private Integer quantity;

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

    @Column(name = "gmt_create")
    private LocalDateTime gmtCreate;
    @Column(name = "gmt_modified")
    private LocalDateTime gmtModified;
}

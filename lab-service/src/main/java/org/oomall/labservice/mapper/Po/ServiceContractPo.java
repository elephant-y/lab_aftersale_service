package org.oomall.labservice.mapper.Po;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "service_contract") // 对应数据库表名
public class ServiceContractPo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自增主键
    private Long id;

    @Column(name = "shop_id", nullable = false) // 商户ID
    private Long shopId;

    @Column(name = "maintainer_id", nullable = false) // 维修人员ID
    private Long maintainerId;

    @Column(name = "region_id", nullable = false) // 区域ID
    private Long regionId;

    @Column(name = "service_type", nullable = false) // 服务类型（tinyint）
    private Integer serviceType;
}
package org.oomall.labservice.mapper.Po;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "maintainer") // 对应数据库表名
public class MaintainerPo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自增主键
    private Long id;

    @Column(name = "maintainer_name", length = 50, nullable = false) // 对应数据库字段
    private String maintainerName;

    @Column(name = "maintainer_mobile", length = 20, nullable = false)
    private String maintainerMobile;

    @Column(name = "maintainer_address", length = 255)
    private String maintainerAddress = ""; // 数据库默认值空字符串
}
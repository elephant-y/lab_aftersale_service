// filePath：labaftersale/controller/dto/AftersaleResponseDto.java
package org.oomall.labaftersale.controller.dto;

import lombok.Data;
import org.oomall.labaftersale.mapper.po.Consignee;
import org.oomall.labaftersale.model.AftersaleStatus;
import org.oomall.labaftersale.model.AftersaleType;
import java.time.LocalDateTime;

@Data
public class AftersaleResponseDto {
    private Long id;
    private Long orderItemId;
    private Long shopId;
    private AftersaleType type; // 保持枚举类型
    private AftersaleStatus status;
    private String conclusion;
    private Integer quantity;
    private Long serviceOrderId;
    private Consignee consignee; // 收件人信息封装为对象
    private LocalDateTime gmtCreate; // 创建时间
    private LocalDateTime gmtModified; // 修改时间
}
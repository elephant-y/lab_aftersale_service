package org.oomall.labaftersale.controller.dto;

import lombok.Data;
import org.oomall.labaftersale.mapper.po.Consignee;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 顾客提交售后申请的请求体DTO（匹配API要求）
 */
@Data
public class AftersaleApplyDto {
    //售后类型：0=退货，1=维修，2=换货（与selectType方法映射）
    @NotNull(message = "售后类型不能为空")
    private Integer type;

    @Min(value = 1, message = "售后数量不能小于1")
    private Integer quantity;

    private String reason;

    @NotNull(message = "收件人信息不能为空")
    private Consignee consignee;
}
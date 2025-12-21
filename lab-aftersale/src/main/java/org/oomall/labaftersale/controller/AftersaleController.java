package org.oomall.labaftersale.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oomall.labaftersale.controller.dto.AftersaleResponseDto;
import org.oomall.labaftersale.mapper.po.AftersalePo;
import org.oomall.labaftersale.controller.dto.AftersaleDto;
import org.oomall.labaftersale.controller.dto.AftersaleApplyDto;
import org.oomall.labaftersale.model.AftersaleStatus;
import org.oomall.labaftersale.model.ReturnNo;
import org.oomall.labaftersale.model.ReturnObject;
import org.oomall.labaftersale.service.AftersaleService;
import org.oomall.labaftersale.service.vo.AftersaleConfirmVo;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class AftersaleController {
    private final AftersaleService aftersaleService;

    @PutMapping("/shops/{shopId}/aftersales/{id}/confirm")
    public ReturnObject confirmAftersale(
            @PathVariable Long shopId,
            @PathVariable Long id,
            @RequestBody AftersaleDto vo) {
        log.info("【售后模块】审核请求 shopId={}, aftersaleId={}, confirm={}", shopId, id, vo);
        return aftersaleService.confirmAftersale(shopId, id, vo);
    }

    @GetMapping("/shops/{shopId}/aftersales/{id}")
    public ReturnObject getAftersale(
            @PathVariable Long shopId,
            @PathVariable Long id) {
        log.info("【售后模块】商户查询售后信息，shopId={}, aftersaleId={}", shopId, id);
        AftersaleResponseDto dto = aftersaleService.getAftersale(shopId, id);
        if (dto == null) {
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST, "售后单不存在或不属于当前商户");
        }
        return new ReturnObject(ReturnNo.OK, aftersaleService.getAftersale(shopId, id));
    }

    /**
     * 顾客提交售后申请（API：POST /orderitems/{id}/aftersales）
     * //@param id PATH参数：order_item_id（订单商品ID）
     */
    @PostMapping("/orderitems/{id}/aftersales")
    public ReturnObject createAftersale(
            @PathVariable("id") Long orderItemId, // PATH参数：order_item_id
            @RequestBody @Valid AftersaleApplyDto applyDto // @Valid 触发参数校验
    ) {
        log.info("【售后模块】顾客提交售后申请，orderItemId={}, 参数={}", orderItemId, applyDto);
        return aftersaleService.createAftersale(orderItemId, applyDto);
    }

    @PutMapping("/shops/{shopId}/aftersales/{id}/status")
    public ReturnObject updateAftersaleStatus(
            @PathVariable Long shopId,
            @PathVariable Long id,
            @RequestParam AftersaleStatus status) { // 接收状态参数（枚举类型）
        log.info("【售后模块】更新售后单状态，shopId={}, aftersaleId={}, 目标状态={}", shopId, id, status);
        return aftersaleService.updateAftersaleStatus(shopId, id, status);
    }
}
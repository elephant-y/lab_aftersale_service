package org.oomall.labaftersale.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oomall.labaftersale.controller.dto.AftersaleDto;
import org.oomall.labaftersale.model.ReturnNo;
import org.oomall.labaftersale.model.ReturnObject;
import org.oomall.labaftersale.service.AftersaleService;
import org.oomall.labaftersale.service.vo.AftersaleConfirmVo;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/shops/{shopId}/aftersales")
@RequiredArgsConstructor
@Slf4j
public class AftersaleController {


    private final AftersaleService aftersaleService;

    @PutMapping("/{id}/confirm")
    public ReturnObject confirmAftersale(@PathVariable Long shopId,
                                         @PathVariable Long id,
                                         @RequestBody AftersaleDto vo) {
        log.info("【售后模块】审核请求 shopId={}, aftersaleId={}, confirm={}", shopId, id, vo);
        return aftersaleService.confirmAftersale(shopId, id, vo);
    }

    @GetMapping("/{id}")
    public ReturnObject getAftersale(@PathVariable Long shopId, @PathVariable Long id) {
        return new ReturnObject(ReturnNo.OK, aftersaleService.getAftersale(shopId, id));
    }
}
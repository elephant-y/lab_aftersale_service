package org.oomall.labservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oomall.labservice.model.ReturnObject;
import org.oomall.labservice.service.MaintainerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/internal/maintainers")
@RequiredArgsConstructor
public class MaintainerController {
    private final MaintainerService maintainerService;

    // 按ID查询维修人员
    @GetMapping("/{id}")
    public ReturnObject findById(@PathVariable Long id) {
        log.info("【服务模块】接收查询维修人员请求，ID={}", id);
        return maintainerService.findById(id);
    }
}
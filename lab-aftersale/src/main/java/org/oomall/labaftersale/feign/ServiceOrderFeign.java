package org.oomall.labaftersale.feign;


import org.oomall.labaftersale.model.ReturnObject;
import org.oomall.labaftersale.service.vo.AftersaleConfirmVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "lab-service")
public interface ServiceOrderFeign {

    @PostMapping("/internal/shops/{shopId}/aftersales/{id}/serviceorders")
    ReturnObject createServiceOrder(
            @PathVariable Long shopId,
            @PathVariable Long id,
            @RequestBody AftersaleConfirmVo vo);
}

package org.oomall.labaftersale.feign;

import org.oomall.labaftersale.model.ReturnObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "service-order-service")
public interface ServiceOrderFeign {

    @PostMapping("/internal/shops/{shopId}/aftersales/{id}/serviceorders")
    ReturnObject createServiceOrder(@PathVariable Long shopId,
                                    @PathVariable Long id);
}

package org.oomall.labservice.feign;



import org.oomall.labservice.feign.po.AftersalePo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "lab-aftersale")
public interface AftersaleFeign {
    @GetMapping("/shops/{shopId}/aftersales/{id}")
    AftersalePo getAftersale(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id")  Long id);
}

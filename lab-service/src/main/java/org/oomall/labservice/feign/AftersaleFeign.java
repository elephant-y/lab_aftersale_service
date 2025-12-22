package org.oomall.labservice.feign;



import org.oomall.labservice.feign.po.AftersalePo;
import org.oomall.labservice.model.AftersaleStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "lab-aftersale")
public interface AftersaleFeign {
    // 查询售后单
    @GetMapping("/shops/{shopId}/aftersales/{id}")
    AftersalePo findAftersaleByShopId(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id")  Long id);

    // 更新售后单状态
    @PutMapping("/shops/{shopId}/aftersales/{id}/status")
    AftersalePo updateAftersaleStatus(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long id,
            @RequestParam("status") AftersaleStatus status);
}

package org.oomall.labservice.feign;


import org.oomall.labservice.feign.dto.AftersaleDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "lab-aftersale")
public interface AftersaleFeign {
    @GetMapping("/internal/aftersales/{id}")
    AftersaleDto getAftersale(@PathVariable("id") Long id);
}

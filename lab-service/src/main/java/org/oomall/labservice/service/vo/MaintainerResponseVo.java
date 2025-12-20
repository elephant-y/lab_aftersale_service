package org.oomall.labservice.service.vo;

import lombok.Data;
import org.oomall.labservice.mapper.Po.MaintainerPo;

@Data
public class MaintainerResponseVo {
    // 嵌套的维修人员对象，字段名与需求中的"maintainer"一致
    private MaintainerPo maintainer;
}
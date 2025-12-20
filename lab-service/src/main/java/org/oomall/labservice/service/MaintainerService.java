package org.oomall.labservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oomall.labservice.dao.MaintainerDao;
import org.oomall.labservice.mapper.Po.MaintainerPo;
import org.oomall.labservice.model.ReturnNo;
import org.oomall.labservice.model.ReturnObject;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintainerService {
    private final MaintainerDao maintainerDao;

    // 仅实现：按ID查询维修人员（含空值处理）
    public ReturnObject findById(Long id) {
        try {
            MaintainerPo maintainer = maintainerDao.findById(id);
            if (maintainer == null) {
                log.error("【服务模块】查询维修人员失败，ID={} 不存在", id);
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }
            log.info("【服务模块】查询维修人员成功，ID={}", id);
            return new ReturnObject(ReturnNo.OK, maintainer);
        } catch (Exception e) {
            log.error("【服务模块】查询维修人员异常，ID={}", id, e);
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
    }
}
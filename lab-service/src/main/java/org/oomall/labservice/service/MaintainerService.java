package org.oomall.labservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oomall.labservice.dao.MaintainerDao;
import org.oomall.labservice.mapper.Po.MaintainerPo;
import org.oomall.labservice.model.ReturnNo;
import org.oomall.labservice.model.ReturnObject;
import org.oomall.labservice.service.vo.MaintainerResponseVo;
import org.springframework.stereotype.Service;

/**
 * 维修人员服务类
 * 提供维修人员相关的业务逻辑处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaintainerService {
    /**
     * 维修人员数据访问对象
     * 用于与数据库交互，实现维修人员数据的持久化操作
     */
    private final MaintainerDao maintainerDao;

    /**
     * 根据ID查询维修人员信息
     *
     * @param id 维修人员ID
     * @return ReturnObject 包含查询结果或错误信息
     *
     * 功能说明：
     * 1. 调用数据访问层查询维修人员信息
     * 2. 处理查询结果为空的情况
     * 3. 记录操作日志
     * 4. 异常处理
     */
    // 仅实现：按ID查询维修人员（含空值处理）
    public ReturnObject findById(Long id) {
        try {
            // 从数据库查询维修人员信息
            MaintainerPo maintainer = maintainerDao.findById(id);
            // 判断查询结果是否为空
            if (maintainer == null) {
                // 记录错误日志：维修人员ID不存在
                log.error("【服务模块】查询维修人员失败，ID={} 不存在", id);
                // 返回错误响应：资源ID不存在
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }
            // 记录成功日志：查询维修人员成功
            log.info("【服务模块】查询维修人员成功，ID={}", id);
            // 用 VO 包装 maintainer 对象
            MaintainerResponseVo responseVo = new MaintainerResponseVo();
            responseVo.setMaintainer(maintainer);

            // 返回成功响应：包含维修人员信息
            return new ReturnObject(ReturnNo.OK, responseVo);
        } catch (Exception e) {
            log.error("【服务模块】查询维修人员异常，ID={}", id, e);
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
    }
}
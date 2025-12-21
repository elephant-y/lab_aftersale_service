package org.oomall.labservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.oomall.labservice.dao.MaintainerDao;
import org.oomall.labservice.mapper.Po.MaintainerPo;
import org.oomall.labservice.mapper.Po.ServiceContractPo;
import org.oomall.labservice.model.*;
import org.oomall.labservice.controller.dto.ServiceOrderDto;
import org.oomall.labservice.dao.ServiceOrderDao;
import org.oomall.labservice.feign.AftersaleFeign;
import org.oomall.labservice.feign.dto.AftersaleDto;
import org.oomall.labservice.feign.po.AftersalePo;
import org.oomall.labservice.mapper.Po.ServiceOrderPo;
import org.oomall.labservice.service.vo.ServiceOrderVo;

import org.springframework.stereotype.Service;


@Slf4j
@RequiredArgsConstructor
@Service
public class ServiceOrderService {
    private final ServiceOrderDao serviceOrderDao;
    private final AftersaleFeign aftersaleFeign;
    private final ServiceContractService serviceContractService;
    private final MaintainerDao maintainerDao;

    public ServiceOrderPo findServiceOrderByShopId(Long shopId, Long id) {
        ServiceOrderPo po = serviceOrderDao.findByIdAndShopId(id, shopId);
        return po;
    }

    public ServiceOrderPo findServiceOrderByMaintainerId(Long maintainerId,Long id) {
        ServiceOrderPo po = serviceOrderDao.findByIdAndMaintainerId(id, maintainerId);
        return po;
    }

    public ReturnObject createServiceOrder(Long shopId, Long aftersaleId, ServiceOrderDto vo) {

        AftersalePo aftersale = aftersaleFeign.getAftersale(shopId, aftersaleId);
        if (aftersale == null) {
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        }

        if (serviceOrderDao.existsByAftersaleId(aftersaleId)) {
            return new ReturnObject(ReturnNo.STATE_NOT_ALLOW);
        }

        ServiceOrderPo po = new ServiceOrderPo();

        po.setShopId(shopId);
        po.setAftersaleId(aftersaleId);
        po.setServiceType(vo.getServiceType());
        po.setConsigneeName(vo.getConsignee().getName());
        po.setConsigneeMobile(vo.getConsignee().getMobile());
        po.setConsigneeRegionId(vo.getConsignee().getRegionId());
        po.setConsigneeAddress(vo.getConsignee().getAddress());

        try {
            serviceOrderDao.insert(po);
        } catch (Exception e) {
            log.error("【服务模块】创建服务单失败", e);
            return new ReturnObject(ReturnNo.SERVICE_ORDER_CREATE_FAIL);
        }
        ServiceOrderVo serviceOrderVo = new ServiceOrderVo();
        serviceOrderVo.setId(po.getId());
        serviceOrderVo.setServiceType(po.getServiceType());
        serviceOrderVo.setConsigneeRegionId(po.getConsigneeRegionId());

        log.info("【服务模块】服务单创建成功，aftersaleId={}", aftersaleId);
        return new ReturnObject(ReturnNo.OK,serviceOrderVo);
    }

    /**
     * 分配服务单给维修人员
     * @param shopId 商户ID
     * @param id 服务单ID
     * @return 处理结果
     */
    public ReturnObject assignServiceOrder(Long shopId, Long id) {
        try {
            // 1. 查询服务单信息
            ServiceOrderPo serviceOrder = serviceOrderDao.findByIdAndShopId(id, shopId);
            if (serviceOrder == null) {
                log.error("【服务模块】服务单不存在，shopId={}, serviceOrderId={}", shopId, id);
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }

            // 2. 校验服务单当前状态是否允许分配
            if (serviceOrder.getStatus() != ServiceOrderStatus.WAITING.ordinal()) {
                log.error("【服务模块】服务单状态不允许分配，当前状态={}, serviceOrderId={}",
                        serviceOrder.getStatus(), id);
                return new ReturnObject(ReturnNo.STATE_NOT_ALLOW);
            }

            // 3. 获取分配所需的区域ID和服务类型
            Integer consigneeRegionId = serviceOrder.getConsigneeRegionId();
            Integer serviceType = serviceOrder.getServiceType();

            if (consigneeRegionId == null || serviceType == null) {
                log.error("【服务模块】服务单信息不完整，无法分配，serviceOrderId={}", id);
                return new ReturnObject(ReturnNo.FIELD_NOTVALID);
            }

            // 4. 查询可分配的服务合同
            ReturnObject contractResult = serviceContractService.findToAssign(shopId, consigneeRegionId.longValue(), serviceType);
            if (contractResult.getErrno() != ReturnNo.OK.getErrNo()) {
                log.error("【服务模块】未找到可分配的服务合同，serviceOrderId={}", id);
                return contractResult;
            }
            ServiceContractPo contract = (ServiceContractPo) contractResult.getData();

            // 5. 查询维修人员信息
            MaintainerPo maintainer = maintainerDao.findById(contract.getMaintainerId());
            if (maintainer == null) {
                log.error("【服务模块】维修人员不存在，maintainerId={}", contract.getMaintainerId());
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }

            // 6. 更新服务单信息
            serviceOrder.setMaintainerId(maintainer.getId());
            serviceOrder.setMaintainerName(maintainer.getMaintainerName());
            serviceOrder.setMaintainerMobile(maintainer.getMaintainerMobile());
            serviceOrder.setMaintainerAddress(maintainer.getMaintainerAddress());
            serviceOrder.setStatus(ServiceOrderStatus.ASSIGNED.ordinal());

            // 7. 保存更新
            serviceOrderDao.insert(serviceOrder); // 假设insert方法兼容更新（JPA的save方法支持）
            log.info("【服务模块】服务单分配成功，serviceOrderId={}, maintainerId={}", id, maintainer.getId());

            return new ReturnObject(ReturnNo.OK, serviceOrder);
        } catch (Exception e) {
            log.error("【服务模块】分配服务单异常，shopId={}, serviceOrderId={}", shopId, id, e);
            return new ReturnObject(ReturnNo.SERVICE_ORDER_CREATE_FAIL);
        }
    }

    /**
     * 维修人员接受服务单
     * @param maintainerId 维修人员ID
     * @param id 服务单ID
     * @return 处理结果
     */
    public ReturnObject acceptServiceOrder(Long maintainerId, Long id) {
        try {
            // 1. 查询服务单信息（验证归属关系）
            ServiceOrderPo serviceOrder = findServiceOrderByMaintainerId(maintainerId, id);
            if (serviceOrder == null) {
                log.error("【服务模块】服务单不存在或不属于该维修人员，serviceOrderId={}, maintainerId={}", id, maintainerId);
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }

            // 2. 校验服务单当前状态是否为已派单
            if (serviceOrder.getStatus() != ServiceOrderStatus.ASSIGNED.ordinal()) {
                log.error("【服务模块】服务单状态不允许接受，当前状态={}, serviceOrderId={}",
                        serviceOrder.getStatus(), id);
                return new ReturnObject(ReturnNo.STATE_NOT_ALLOW);
            }

            // 3. 获取服务单类型
            Integer serviceType = serviceOrder.getServiceType();
            ServiceOrderType orderType;
            try {
                orderType = ServiceOrderType.values()[serviceType];
            } catch (ArrayIndexOutOfBoundsException e) {
                log.error("【服务模块】服务单类型不合法，serviceType={}, serviceOrderId={}", serviceType, id);
                return new ReturnObject(ReturnNo.AFTERSALE_TYPE_ERROR);
            }

            // 4. 根据服务类型创建对应服务实例并执行
            FatherService service;
            if (ServiceOrderType.SITEMENT.equals(orderType)) {
                service = new SiteService(serviceOrder);
            } else if (ServiceOrderType.SHIPMENT.equals(orderType)) {
                service = new ShipService(serviceOrder);
            } else {
                log.error("【服务模块】不支持的服务类型，serviceType={}, serviceOrderId={}", serviceType, id);
                return new ReturnObject(ReturnNo.AFTERSALE_TYPE_ERROR);
            }

            // 执行公共逻辑（状态改为ACCEPTED）和子类特有逻辑
            service.execute();

            // 5. 保存更新
            serviceOrderDao.insert(serviceOrder);
            log.info("【服务模块】服务单接受成功，serviceOrderId={}, maintainerId={}", id, maintainerId);

            return new ReturnObject(ReturnNo.OK, serviceOrder);
        } catch (Exception e) {
            log.error("【服务模块】接受服务单异常，serviceOrderId={}, maintainerId={}", id, maintainerId, e);
            return new ReturnObject(ReturnNo.SERVICE_ORDER_CREATE_FAIL);
        }
    }

    /**
     * 维修人员拒绝服务单
     * @param maintainerId 维修人员ID
     * @param id 服务单ID
     * @return 处理结果
     */
    public ReturnObject refuseServiceOrder(Long maintainerId, Long id) {
        try {
            // 1. 查询服务单信息（验证归属关系）
            ServiceOrderPo serviceOrder = findServiceOrderByMaintainerId(maintainerId, id);
            if (serviceOrder == null) {
                log.error("【服务模块】服务单不存在或不属于该维修人员，serviceOrderId={}, maintainerId={}", id, maintainerId);
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }

            // 2. 校验服务单当前状态是否为已派单
            if (serviceOrder.getStatus() != ServiceOrderStatus.ASSIGNED.ordinal()) {
                log.error("【服务模块】服务单状态不允许拒绝，当前状态={}, serviceOrderId={}",
                        serviceOrder.getStatus(), id);
                return new ReturnObject(ReturnNo.STATE_NOT_ALLOW);
            }

            // 3. 更新服务单状态为已拒单
            serviceOrder.setStatus(ServiceOrderStatus.REJECTED.ordinal());

            // 4. 保存更新
            serviceOrderDao.insert(serviceOrder); // 复用现有保存方法
            log.info("【服务模块】服务单拒绝成功，serviceOrderId={}, maintainerId={}", id, maintainerId);

            return new ReturnObject(ReturnNo.OK, serviceOrder);
        } catch (Exception e) {
            log.error("【服务模块】拒绝服务单异常，serviceOrderId={}, maintainerId={}", id, maintainerId, e);
            return new ReturnObject(ReturnNo.SERVICE_ORDER_CREATE_FAIL);
        }
    }

    /**
     * 维修人员接收产品（寄件服务）
     */
    public ReturnObject receiveProduct(Long maintainerId, Long orderId) {
        try {
            // 1. 校验服务单归属和存在性
            ServiceOrderPo serviceOrder = findServiceOrderByMaintainerId(maintainerId, orderId);
            if (serviceOrder == null) {
                log.error("【服务模块】服务单不存在或不属于该维修人员，serviceOrderId={}, maintainerId={}", orderId, maintainerId);
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }

            // 2. 校验服务类型是否为寄件
            Integer serviceType = serviceOrder.getServiceType();
            if (serviceType == null || !ServiceOrderType.SHIPMENT.equals(ServiceOrderType.values()[serviceType])) {
                log.error("【服务模块】非寄件服务单不支持接收产品，serviceOrderId={}", orderId);
                return new ReturnObject(ReturnNo.AFTERSALE_TYPE_ERROR);
            }

            // 3. 校验当前状态是否允许接收（必须是已接单状态）
            if (serviceOrder.getStatus() != ServiceOrderStatus.ACCEPTED.ordinal()) {
                log.error("【服务模块】服务单状态不允许接收产品，当前状态={}, serviceOrderId={}", serviceOrder.getStatus(), orderId);
                return new ReturnObject(ReturnNo.STATE_NOT_ALLOW);
            }

            // 4. 执行接收产品逻辑（更新状态为已签收）
            serviceOrder.setStatus(ServiceOrderStatus.RECEIVED.ordinal());
            serviceOrderDao.insert(serviceOrder); // 保存更新
            log.info("【寄件服务】产品已签收，serviceOrderId={}", orderId);

            return new ReturnObject(ReturnNo.OK, serviceOrder);
        } catch (Exception e) {
            log.error("【服务模块】接收产品异常，serviceOrderId={}, maintainerId={}", orderId, maintainerId, e);
            return new ReturnObject(ReturnNo.SERVICE_ORDER_CREATE_FAIL);
        }
    }

    /**
     * 维修人员发送产品（寄件服务）
     */
    public ReturnObject sendProduct(Long maintainerId, Long orderId) {
        try {
            // 1. 校验服务单归属和存在性
            ServiceOrderPo serviceOrder = findServiceOrderByMaintainerId(maintainerId, orderId);
            if (serviceOrder == null) {
                log.error("【服务模块】服务单不存在或不属于该维修人员，serviceOrderId={}, maintainerId={}", orderId, maintainerId);
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }

            // 2. 校验服务类型是否为寄件
            Integer serviceType = serviceOrder.getServiceType();
            if (serviceType == null || !ServiceOrderType.SHIPMENT.equals(ServiceOrderType.values()[serviceType])) {
                log.error("【服务模块】非寄件服务单不支持发送产品，serviceOrderId={}", orderId);
                return new ReturnObject(ReturnNo.AFTERSALE_TYPE_ERROR);
            }

            // 3. 校验当前状态是否允许发送（必须是已签收状态）
            if (serviceOrder.getStatus() != ServiceOrderStatus.RECEIVED.ordinal()) {
                log.error("【服务模块】服务单状态不允许发送产品，当前状态={}, serviceOrderId={}", serviceOrder.getStatus(), orderId);
                return new ReturnObject(ReturnNo.STATE_NOT_ALLOW);
            }

            // 4. 执行发送产品逻辑（这里仅做日志示例，实际可补充物流对接等逻辑）
            log.info("【寄件服务】准备从[{}]发送到[{}]，serviceOrderId={}",
                    serviceOrder.getMaintainerAddress(), serviceOrder.getConsigneeAddress(), orderId);

            // 5. 更新服务单状态为已发货
            serviceOrder.setStatus(ServiceOrderStatus.SENT.ordinal());
            serviceOrderDao.insert(serviceOrder);

            return new ReturnObject(ReturnNo.OK, serviceOrder);
        } catch (Exception e) {
            log.error("【服务模块】发送产品异常，serviceOrderId={}, maintainerId={}", orderId, maintainerId, e);
            return new ReturnObject(ReturnNo.SERVICE_ORDER_CREATE_FAIL);
        }
    }

    /**
     * 维修人员完成服务单
     * @param maintainerId 维修人员ID
     * @param id 服务单ID
     * @return 处理结果
     */
    public ReturnObject finishServiceOrder(Long maintainerId, Long id) {
        try {
            // 1. 查询服务单信息（验证归属关系）
            ServiceOrderPo serviceOrder = findServiceOrderByMaintainerId(maintainerId, id);
            if (serviceOrder == null) {
                log.error("【服务模块】服务单不存在或不属于该维修人员，serviceOrderId={}, maintainerId={}", id, maintainerId);
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }

            // 2. 获取服务单类型和当前状态
            Integer serviceType = serviceOrder.getServiceType();
            Integer currentStatus = serviceOrder.getStatus();

            // 3. 验证服务类型是否合法
            ServiceOrderType orderType;
            try {
                orderType = ServiceOrderType.values()[serviceType];
            } catch (ArrayIndexOutOfBoundsException e) {
                log.error("【服务模块】服务单类型不合法，serviceType={}, serviceOrderId={}", serviceType, id);
                return new ReturnObject(ReturnNo.AFTERSALE_TYPE_ERROR);
            }

            // 4. 根据服务类型验证当前状态是否允许完成
            boolean canFinish = false;
            if (ServiceOrderType.SITEMENT.equals(orderType)) {
                // 上门服务需处于已接单状态
                canFinish = currentStatus == ServiceOrderStatus.ACCEPTED.ordinal();
            } else if (ServiceOrderType.SHIPMENT.equals(orderType)) {
                // 寄件服务需处于已签收状态
                canFinish = currentStatus == ServiceOrderStatus.SENT.ordinal();
            } else {
                // 其他服务类型不支持完成操作
                log.error("【服务模块】服务单类型不支持完成操作，serviceType={}, serviceOrderId={}", serviceType, id);
                return new ReturnObject(ReturnNo.AFTERSALE_TYPE_ERROR);
            }

            if (!canFinish) {
                log.error("【服务模块】服务单状态不允许完成，当前状态={}, 服务类型={}, serviceOrderId={}",
                        currentStatus, orderType, id);
                return new ReturnObject(ReturnNo.STATE_NOT_ALLOW);
            }

            // 5. 更新服务单状态为已完成
            serviceOrder.setStatus(ServiceOrderStatus.FINISHED.ordinal());

            // 6. 保存更新
            serviceOrderDao.insert(serviceOrder);
            log.info("【服务模块】服务单完成成功，serviceOrderId={}, maintainerId={}", id, maintainerId);

            // 7. 同步更新对应售后单状态为COMPLETED
            Long aftersaleId = serviceOrder.getAftersaleId();
            Long shopId = serviceOrder.getShopId();
            if (aftersaleId != null && shopId != null) {
                try {
                    // 调用Feign更新售后单状态
                    aftersaleFeign.updateAftersaleStatus(
                            shopId,
                            aftersaleId,
                            AftersaleStatus.COMPLETED
                    );
                    log.info("【服务模块】同步售后单状态为COMPLETED成功，aftersaleId={}", aftersaleId);
                } catch (Exception e) {
                    // 记录Feign调用异常，但不影响服务单本身的完成状态
                    log.error("【服务模块】同步售后单状态失败，aftersaleId={}", aftersaleId, e);
                }
            } else {
                log.warn("【服务模块】服务单未关联售后单，无需同步状态，serviceOrderId={}", id);
            }
            return new ReturnObject(ReturnNo.OK, serviceOrder);
        } catch (Exception e) {
            log.error("【服务模块】完成服务单异常，serviceOrderId={}, maintainerId={}", id, maintainerId, e);
            return new ReturnObject(ReturnNo.SERVICE_ORDER_CREATE_FAIL);
        }
    }


}

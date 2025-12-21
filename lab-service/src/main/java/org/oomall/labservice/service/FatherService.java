package org.oomall.labservice.service;

import org.oomall.labservice.mapper.Po.ServiceOrderPo;
import org.oomall.labservice.model.ServiceOrderStatus;
import lombok.extern.slf4j.Slf4j;

// 抽象服务类
public abstract class FatherService {
    protected ServiceOrderPo serviceOrder;

    protected FatherService(ServiceOrderPo serviceOrder) {
        this.serviceOrder = serviceOrder;
    }

    // 公共逻辑：将状态改为ACCEPTED
    public void execute() {
        serviceOrder.setStatus(ServiceOrderStatus.ACCEPTED.ordinal());
        // 执行子类特有逻辑
        doExecute();
    }

    // 子类实现特有逻辑
    protected abstract void doExecute();
}

// 上门服务子类
@Slf4j
class SiteService extends FatherService {
    public SiteService(ServiceOrderPo serviceOrder) {
        super(serviceOrder);
    }

    @Override
    protected void doExecute() {
        // 上门服务特有逻辑（暂空）
    }
}

// 寄件服务子类
@Slf4j
class ShipService extends FatherService {
    public ShipService(ServiceOrderPo serviceOrder) {
        super(serviceOrder);
    }

    // 寄件服务特有逻辑
    @Override
    protected void doExecute() {
        log.info("【寄件服务】服务单已被接受，等待维修人员接收产品，serviceOrderId={}", serviceOrder.getId());
    }

    /**
     * 接收产品，将服务单状态设为已签收
     */
    private void receiveProduct() {
        serviceOrder.setStatus(ServiceOrderStatus.RECEIVED.ordinal());
        log.info("【寄件服务】产品已签收，服务单ID={}", serviceOrder.getId());
    }

    /**
     * 发送产品（暂为空实现，后续补充）
     * @param maintainerAddress 维修人员地址（发货地址）
     * @param consigneeAddress 收件人地址（收货地址）
     */
    private void send(String maintainerAddress, String consigneeAddress) {
        // 后续补充发送逻辑
        log.info("【寄件服务】准备从[{}]发送到[{}]，服务单ID={}",
                maintainerAddress, consigneeAddress, serviceOrder.getId());
    }
}
package org.oomall.labaftersale.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.oomall.labaftersale.controller.dto.AftersaleApplyDto;
import org.oomall.labaftersale.controller.dto.AftersaleDto;
import org.oomall.labaftersale.controller.dto.AftersaleResponseDto;
import org.oomall.labaftersale.dao.AftersaleDao;
import org.oomall.labaftersale.feign.ServiceOrderFeign;
import org.oomall.labaftersale.mapper.po.AftersalePo;
import org.oomall.labaftersale.mapper.po.Consignee;
import org.oomall.labaftersale.model.AftersaleStatus;
import org.oomall.labaftersale.model.AftersaleType;
import org.oomall.labaftersale.model.ReturnNo;
import org.oomall.labaftersale.model.ReturnObject;
import org.oomall.labaftersale.model.ServiceOrderType;
import org.oomall.labaftersale.service.vo.AftersaleConfirmVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class AftersaleService {

    private final AftersaleDao aftersaleDao;
    private final ServiceOrderFeign serviceOrderFeign;

    // ===== Redis 缓存 =====
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    // ===== Redisson 分布式锁 =====
    private final RedissonClient redissonClient;

    /** 缓存 key 前缀，格式：aftersale:shop:{shopId}:{id} */
    private static final String CACHE_KEY_PREFIX = "aftersale:shop:";
    /** 缓存过期时间：10 分钟（写操作会主动删缓存，TTL 只是兜底） */
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    /** 组装缓存 key */
    private String cacheKey(Long shopId, Long id) {
        return CACHE_KEY_PREFIX + shopId + ":" + id;
    }

    /** 删除缓存（缓存属于旁路增强，失败不能影响主业务） */
    private void evictCache(Long shopId, Long id) {
        try {
            stringRedisTemplate.delete(cacheKey(shopId, id));
        } catch (Exception e) {
            log.warn("【售后模块】删除缓存失败，shopId={}, id={}", shopId, id, e);
        }
    }

    public AftersaleResponseDto findAftersaleByShopId(Long shopId, Long id) {
        String key = cacheKey(shopId, id);

        // 1. 先查缓存（Redis 故障时降级为直接查库，不影响主业务）
        String cached = null;
        try {
            cached = stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("【售后模块】读取缓存失败，降级查库，key={}", key, e);
        }
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, AftersaleResponseDto.class);
            } catch (Exception e) {
                // 反序列化失败就当作未命中，回源数据库
                log.warn("【售后模块】缓存反序列化失败，回源数据库，key={}", key, e);
            }
        }

        // 2. 缓存未命中，查数据库
        AftersalePo po = aftersaleDao.findByIdAndShopId(id, shopId);
        if (po == null) {
            return null;
        }
        AftersaleResponseDto dto = convertToResponseDto(po);

        // 3. 回填缓存（写缓存失败不能影响主流程）
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(dto), CACHE_TTL);
        } catch (Exception e) {
            log.warn("【售后模块】写入缓存失败，key={}", key, e);
        }
        return dto;
    }

    /**
     * 顾客提交售后申请，创建售后单（状态=APPLY）
     * @param orderItemId 订单商品ID（PATH参数）
     * @param applyDto 请求体参数
     */
    @Transactional
    public ReturnObject createAftersale(Long orderItemId, AftersaleApplyDto applyDto) {
        // 1. 基础参数校验
        if (orderItemId == null) {
            log.error("【售后模块】创建售后单失败：订单商品ID不能为空");
            return new ReturnObject(ReturnNo.FIELD_NOTVALID, "订单商品ID不能为空");
        }

        // 2. 校验售后数量和原因
        if (applyDto.getQuantity() == null || applyDto.getQuantity() < 1) {
            log.error("【售后模块】创建售后单失败：售后数量不合法，orderItemId={}", orderItemId);
            return new ReturnObject(ReturnNo.FIELD_NOTVALID, "售后数量不能小于1");
        }
        if (applyDto.getReason() == null || applyDto.getReason().trim().isEmpty()) {
            log.error("【售后模块】创建售后单失败：售后原因不能为空，orderItemId={}", orderItemId);
            return new ReturnObject(ReturnNo.FIELD_NOTVALID, "售后原因不能为空");
        }

        // 3. 收件人信息校验（姓名+电话必填）
        Consignee consignee = applyDto.getConsignee();
        if (consignee == null || consignee.getName() == null || consignee.getMobile() == null) {
            log.error("【售后模块】创建售后单失败：收件人姓名/电话不能为空，orderItemId={}", orderItemId);
            return new ReturnObject(ReturnNo.FIELD_NOTVALID, "收件人姓名和电话为必填项");
        }
        try
        {
             Boolean first = stringRedisTemplate.opsForValue()
            .setIfAbsent("aftersale:apply:" + orderItemId, "1", Duration.ofSeconds(10));
            if (Boolean.FALSE.equals(first)) {
                return new ReturnObject(ReturnNo.STATE_NOT_ALLOW, "该订单商品已提交售后，请勿重复申请");
            }
        }
        catch(Exception e)
        {
            log.warn("【售后模块】redis异常");
        }
        // 4. 校验是否已提交过售后（通过order_item_id唯一约束）
        if (aftersaleDao.existsByOrderItemId(orderItemId)) {
            log.error("【售后模块】创建售后单失败：该订单商品已提交售后，orderItemId={}", orderItemId);
            return new ReturnObject(ReturnNo.STATE_NOT_ALLOW, "该订单商品已提交售后，请勿重复申请");
        }

        AftersaleType aftersaleType = AftersaleType.fromInt(applyDto.getType());
        if (aftersaleType == null) {
            log.error("【售后模块】创建售后单失败：售后类型不合法，orderItemId={}", orderItemId);
            return new ReturnObject(ReturnNo.AFTERSALE_TYPE_ERROR);
        }

        // 5. 构造售后单Po（状态=APPLIED）
        AftersalePo aftersalePo = new AftersalePo();
        aftersalePo.setOrderItemId(orderItemId); // 关联订单商品ID
        aftersalePo.setShopId(1L); // 若有店铺ID来源（如从订单商品查询）可替换，暂无则默认1
        aftersalePo.setStatus(AftersaleStatus.APPLIED); // 初始状态：申请中
        aftersalePo.setType(AftersaleType.fromInt(applyDto.getType())); // 转换为AftersaleType枚举
        aftersalePo.setQuantity(applyDto.getQuantity()); // 售后数量
        aftersalePo.setConclusion(applyDto.getReason() == null ? "" : applyDto.getReason()); // 售后原因存入conclusion字段
        // 填充收件人信息
        aftersalePo.setConsigneeName(consignee.getName());
        aftersalePo.setConsigneeMobile(consignee.getMobile());
        aftersalePo.setConsigneeRegionId(consignee.getRegionId());
        aftersalePo.setConsigneeAddress(consignee.getAddress());
        aftersalePo.setServiceOrderId(null); // 未创建服务单，默认为null

        aftersalePo.setGmtCreate(LocalDateTime.now()); // 设置创建时间
        aftersalePo.setGmtModified(LocalDateTime.now()); // 设置修改时间
        // 6. 入库保存
        try {
            aftersaleDao.save(aftersalePo);
            log.info("【售后模块】售后单创建成功，orderItemId={}, 售后单ID={}, 状态=APPLY",
                    orderItemId, aftersalePo.getId());
        } catch (Exception e) {
            // 入库失败：删除防重标记，让用户可以立即重试（否则要干等 10 秒 TTL 过期）
            // 删除也要 try-catch：Redis 挂了不能让删除异常盖掉原来的入库错误
            try {
                stringRedisTemplate.delete("aftersale:apply:" + orderItemId);
            } catch (Exception ex) {
                log.warn("【售后模块】删除防重标记失败，orderItemId={}", orderItemId, ex);
            }
            log.error("【售后模块】创建售后单入库失败，orderItemId={}", orderItemId, e);
            return new ReturnObject(ReturnNo.SERVICE_ORDER_CREATE_FAIL, "售后单创建失败");
        }

        // 7. 返回结果（包含售后单ID）
        return new ReturnObject(aftersalePo.getId());
    }

    @Transactional
    public ReturnObject confirmAftersale(Long shopId, Long id, AftersaleDto dto) {
        // 参数校验
        if (dto == null) {
            return new ReturnObject(ReturnNo.FIELD_NOTVALID);
        }

        // ===== 分布式锁（Redisson）：防止并发审核同一张售后单 =====
        RLock lock = redissonClient.getLock("aftersale:lock:" + id);
        boolean locked;   // 是否放行执行
        boolean acquired; // 是否真正持有锁（决定 finally 是否需要释放）
        try {
            // waitTime=0：不排队，拿不到立即失败；leaseTime=-1：启用看门狗自动续期
            acquired = lock.tryLock(0, -1, TimeUnit.SECONDS);
            locked = acquired;
        } catch (Exception e) {
            // Redis 故障降级放行（锁只是并发保护，不能因 Redis 挂了阻塞正常审核）
            log.warn("【售后模块】获取分布式锁失败，降级继续执行，aftersaleId={}", id, e);
            acquired = false;
            locked = true;
        }
        if (!locked) {
            return new ReturnObject(ReturnNo.STATE_NOT_ALLOW, "售后单正在处理中，请勿重复操作");
        }

        try {
            // 校验售后类型是否合法
            AftersaleType aftersaleType = AftersaleType.fromInt(dto.getType());
            if (aftersaleType == null) {
                log.error("【售后模块】审核售后单失败：售后类型不合法，id={}, type={}", id, dto.getType());
                return new ReturnObject(ReturnNo.AFTERSALE_TYPE_ERROR, "售后类型不合法");
            }

            // 查询售后单
            AftersalePo po = aftersaleDao.findByIdAndShopId(id, shopId);
            if (po == null) {
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }

            // 状态校验
            if (!AftersaleStatus.APPLIED.equals(po.getStatus())) {
                return new ReturnObject(ReturnNo.STATE_NOT_ALLOW);
            }

            if (dto.isConfirm() && aftersaleType == AftersaleType.REPAIR) {
                if (dto.getServiceType() == null || (ServiceOrderType.fromInt(dto.getServiceType()) != ServiceOrderType.SITEMENT && ServiceOrderType.fromInt(dto.getServiceType()) != ServiceOrderType.SHIPMENT)) {
                    return new ReturnObject(ReturnNo.SERVICE_TYPE_ERROR, "服务单类型必须为0（上门）或1（寄件）");
                }
            }

            // 处理审核结果
            handleAftersaleReview(po, dto, shopId);
            po.setGmtModified(LocalDateTime.now()); // 更新修改时间

            // 保存更新后的售后单
            aftersaleDao.save(po);

            // 先更新数据库，成功后删除缓存，保证下次查询读到最新数据
            evictCache(shopId, id);

            return new ReturnObject(ReturnNo.OK);
        } finally {
            if (acquired) {
                // 关键：锁必须在【事务提交之后】释放。
                // 若在 finally 直接释放，锁先于事务提交释放，别的请求会拿到锁并读到未提交的旧数据，
                // 导致并发校验失效。因此这里只注册回调，由 Spring 在事务结束（提交或回滚）后触发释放。
                // Redisson 的 unlock 内部用 Lua 校验持有者并原子删除，误删他人锁的问题由它解决。
                try {
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCompletion(int status) {
                            if (lock.isHeldByCurrentThread()) {
                                lock.unlock();
                            }
                        }
                    });
                } catch (Exception e) {
                    // 没有活跃事务同步时（异常路径），退化为立即释放
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
        }
    }

    /**
     * 处理售后单审核结果
     */
    private void handleAftersaleReview(AftersalePo po, AftersaleDto dto, Long shopId) {
        // 设置审核结论
        po.setConclusion(dto.getConclusion());
        AftersaleConfirmVo vo = new AftersaleConfirmVo();
        vo.setType(dto.getType());
        Consignee consignee = new Consignee();
        consignee.setName(po.getConsigneeName());
        consignee.setMobile(po.getConsigneeMobile());
        consignee.setRegionId(po.getConsigneeRegionId());
        consignee.setAddress(po.getConsigneeAddress());
        vo.setConsignee(consignee);
        vo.setServiceType(dto.getServiceType());

        // 审核拒绝
        if (!dto.isConfirm()) {
            po.setStatus(AftersaleStatus.REJECTED);
            po.setServiceOrderId(null);
            po.setType(AftersaleType.fromInt(dto.getType()));
            log.info("【售后模块】审核拒绝售后单id={}", po.getId());
            return;
        }

        // 审核通过
        // 设置售后类型
        po.setType(AftersaleType.fromInt(vo.getType()));
        // 根据售后类型获取对应的处理器
        AftersaleHandler handler;
        if (po.getType() == AftersaleType.REFUNDONLY) {
            handler = new RefundOnlyHandler(shopId, po, vo);
            handler.execute(); // 执行处理逻辑
        } if (po.getType() == AftersaleType.RETURN) {
            handler = new ReturnHandler(shopId, po, vo);
            handler.execute();
        } if (po.getType() == AftersaleType.REPAIR) {
            handler = new RepairHandler(shopId, po, vo, serviceOrderFeign);
            handler.execute();
        }
    }

    /**
     * 更新售后单状态
     */
    public ReturnObject updateAftersaleStatus(Long shopId, Long id, AftersaleStatus status) {
        // 1. 查询售后单是否存在且属于当前店铺
        AftersalePo po = aftersaleDao.findByIdAndShopId(id, shopId);
        if (po == null) {
            log.error("【售后模块】更新状态失败：售后单不存在或不属于当前商户，shopId={}, aftersaleId={}", shopId, id);
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST, "售后单不存在或不属于当前商户");
        }

        // 2. 校验状态转换是否合法
        if (!isStatusTransitionValid(po.getStatus(), status)) {
            log.error("【售后模块】更新状态失败：状态转换不合法，当前状态={}, 目标状态={}, aftersaleId={}",
                    po.getStatus(), status, id);
            return new ReturnObject(ReturnNo.STATE_NOT_ALLOW, "当前状态不允许转换为目标状态");
        }

        // 3. 更新状态和修改时间
        po.setStatus(status);
        po.setGmtModified(LocalDateTime.now());
        aftersaleDao.save(po);

        // 状态变更后删除缓存（此方法也会被 lab-service 通过 Feign 回调）
        evictCache(shopId, id);

        log.info("【售后模块】售后单状态更新成功，aftersaleId={}, 新状态={}", id, status);
        return new ReturnObject(ReturnNo.OK);
    }

    /**
     * 校验状态转换是否合法
     */
    private boolean isStatusTransitionValid(AftersaleStatus currentStatus, AftersaleStatus targetStatus) {
        // 只有处理中（PROCESSING）可以转为已完成（COMPLETED）
        if (currentStatus == AftersaleStatus.PROCESSING && targetStatus == AftersaleStatus.COMPLETED) {
            return true;
        }
        // 其他合法转换规则可在此添加
        return false;
    }

    // 实体转DTO的方法
    private AftersaleResponseDto convertToResponseDto(AftersalePo po) {
        AftersaleResponseDto dto = new AftersaleResponseDto();
        dto.setId(po.getId());
        dto.setOrderItemId(po.getOrderItemId());
        dto.setShopId(po.getShopId());
        dto.setType(po.getType());
        dto.setStatus(po.getStatus());
        dto.setConclusion(po.getConclusion());
        dto.setQuantity(po.getQuantity());
        dto.setServiceOrderId(po.getServiceOrderId());
        dto.setGmtCreate(po.getGmtCreate());
        dto.setGmtModified(po.getGmtModified());

        // 封装收件人信息为Consignee对象
        Consignee consignee = new Consignee();
        consignee.setName(po.getConsigneeName());
        consignee.setMobile(po.getConsigneeMobile());
        consignee.setRegionId(po.getConsigneeRegionId());
        consignee.setAddress(po.getConsigneeAddress());
        dto.setConsignee(consignee);

        return dto;
    }

}
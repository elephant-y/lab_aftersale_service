-- ============================================
-- 本地开发环境初始化脚本（Docker MySQL 8）
-- 用法: docker exec -i mysql mysql -uroot -p<你的本地MySQL密码> --default-character-set=utf8mb4 < scripts/init-local-db.sql
-- 注意: 必须带 --default-character-set=utf8mb4 并 SET NAMES,否则中文会双重编码
-- ============================================
SET NAMES utf8mb4;

-- ---------- lab-aftersale 售后库 ----------
CREATE DATABASE IF NOT EXISTS aftersale_db DEFAULT CHARACTER SET utf8mb4;
USE aftersale_db;

CREATE TABLE IF NOT EXISTS aftersale (
    id                  BIGINT NOT NULL AUTO_INCREMENT,
    order_item_id       BIGINT NOT NULL,
    shop_id             BIGINT,
    type                INT,
    status              INT,
    conclusion          VARCHAR(255),
    quantity            INT,
    service_order_id    BIGINT,
    consignee_name      VARCHAR(100),
    consignee_mobile    VARCHAR(50),
    consignee_region_id BIGINT,
    consignee_address   VARCHAR(255),
    gmt_create          DATETIME(6),
    gmt_modified        DATETIME(6),
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------- lab-service 服务库 ----------
CREATE DATABASE IF NOT EXISTS service_db DEFAULT CHARACTER SET utf8mb4;
USE service_db;

CREATE TABLE IF NOT EXISTS service_order (
    id                  BIGINT NOT NULL AUTO_INCREMENT,
    shop_id             BIGINT NOT NULL,
    aftersale_id        BIGINT,
    service_type        INT,
    consignee_name      VARCHAR(100),
    consignee_mobile    VARCHAR(50),
    consignee_region_id INT,
    consignee_address   VARCHAR(255),
    status              INT DEFAULT 0,
    maintainer_id       BIGINT,
    maintainer_name     VARCHAR(50),
    maintainer_mobile   VARCHAR(20),
    maintainer_address  VARCHAR(255),
    gmt_create          DATETIME(6),
    gmt_modified        DATETIME(6),
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS maintainer (
    id                 BIGINT NOT NULL AUTO_INCREMENT,
    maintainer_name    VARCHAR(50) NOT NULL,
    maintainer_mobile  VARCHAR(20) NOT NULL,
    maintainer_address VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS service_contract (
    id            BIGINT NOT NULL AUTO_INCREMENT,
    shop_id       BIGINT NOT NULL,
    maintainer_id BIGINT NOT NULL,
    region_id     BIGINT NOT NULL,
    service_type  INT NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------- 种子数据（跑通完整流程用） ----------
-- 1 个维修人员 + 2 份合同（区域 100，覆盖上门 0 / 寄件 1 两种服务类型）
INSERT INTO maintainer (maintainer_name, maintainer_mobile, maintainer_address)
SELECT '王师傅', '13800001111', '厦门市思明区'
WHERE NOT EXISTS (SELECT 1 FROM maintainer WHERE id = 1);

INSERT INTO service_contract (shop_id, maintainer_id, region_id, service_type)
SELECT 1, 1, 100, 0
WHERE NOT EXISTS (SELECT 1 FROM service_contract WHERE shop_id = 1 AND region_id = 100 AND service_type = 0);

INSERT INTO service_contract (shop_id, maintainer_id, region_id, service_type)
SELECT 1, 1, 100, 1
WHERE NOT EXISTS (SELECT 1 FROM service_contract WHERE shop_id = 1 AND region_id = 100 AND service_type = 1);

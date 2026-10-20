-- 修复种子数据的中文双重编码问题（用 --default-character-set=utf8mb4 执行）
SET NAMES utf8mb4;

UPDATE service_db.maintainer
SET maintainer_name = '王师傅',
    maintainer_address = '厦门市思明区'
WHERE id = 1;

UPDATE service_db.service_order
SET maintainer_name = '王师傅',
    maintainer_address = '厦门市思明区'
WHERE id = 1;

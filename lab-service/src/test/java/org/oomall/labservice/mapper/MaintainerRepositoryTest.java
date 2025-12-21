package org.oomall.labservice.mapper;



import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oomall.labservice.mapper.Po.MaintainerPo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest  // 只加载 JPA 组件，使用 H2 内存数据库
class MaintainerRepositoryTest {

    @Autowired
    private MaintainerRepository maintainerRepository;

    private static final Long MAINTAINER_ID = 400L;

    // 构建一个模拟的 MaintainerPo 对象
    private MaintainerPo buildMaintainerPo() {
        MaintainerPo po = new MaintainerPo();
        po.setId(null); // 保存前 ID 为 null
        po.setMaintainerName("张师傅");
        po.setMaintainerMobile("13800138000");
        po.setMaintainerAddress("北京市朝阳区");
        // 根据你的 MaintainerPo 实际字段补充其他属性（如 status、gmtCreate 等）
        return po;
    }

    @Test
    @DisplayName("save - 正常保存维修人员并自动生成 ID")
    void save_shouldPersistAndAssignId() {
        // Given: 一个新实体，ID 为 null
        MaintainerPo po = buildMaintainerPo();

        // When: 保存
        MaintainerPo saved = maintainerRepository.save(po);

        // Then: 保存成功，ID 被自动生成且不为 null
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isPositive();

        // 验证数据库中确实存在该记录
        assertThat(maintainerRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    @DisplayName("findById - 存在时返回 Optional 包含对象")
    void findById_shouldReturnPresentOptional_whenExists() {
        // Given: 先保存一条记录
        MaintainerPo po = buildMaintainerPo();
        MaintainerPo saved = maintainerRepository.save(po);

        // When
        Optional<MaintainerPo> result = maintainerRepository.findById(saved.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(saved);
        assertThat(result.get().getMaintainerName()).isEqualTo("张师傅");
    }

    @Test
    @DisplayName("findById - 不存在时返回 Optional.empty()")
    void findById_shouldReturnEmptyOptional_whenNotExists() {
        // When
        Optional<MaintainerPo> result = maintainerRepository.findById(999L); // 不存在的 ID

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("save - 更新现有实体")
    void save_shouldUpdateExistingEntity() {
        // Given: 保存一条记录
        MaintainerPo po = buildMaintainerPo();
        MaintainerPo saved = maintainerRepository.save(po);

        // 修改字段
        saved.setMaintainerName("李师傅");
        saved.setMaintainerMobile("13900139000");

        // When: 再次保存（更新）
        MaintainerPo updated = maintainerRepository.save(saved);

        // Then: 更新成功，ID 不变
        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getMaintainerName()).isEqualTo("李师傅");

        // 从数据库重新查询验证
        Optional<MaintainerPo> queried = maintainerRepository.findById(saved.getId());
        assertThat(queried).isPresent();
        assertThat(queried.get().getMaintainerName()).isEqualTo("李师傅");
    }
}
package org.oomall.labservice.dao;



import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oomall.labservice.mapper.MaintainerRepository;
import org.oomall.labservice.mapper.Po.MaintainerPo;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintainerDaoTest {

    @Mock
    private MaintainerRepository maintainerRepository;

    @InjectMocks
    private MaintainerDao maintainerDao;

    private static final Long EXISTING_ID = 100L;
    private static final Long NOT_EXIST_ID = 999L;

    // 构建一个模拟的 MaintainerPo
    private MaintainerPo buildMaintainerPo() {
        MaintainerPo po = new MaintainerPo();
        po.setId(EXISTING_ID);
        po.setMaintainerName("张师傅");
        po.setMaintainerMobile("13800138000");
        // 根据你的 PO 类实际字段补充其他属性
        return po;
    }

    @Test
    @DisplayName("根据ID查询维修人员 - 存在时返回 Po 对象")
    void findById_shouldReturnPo_whenIdExists() {
        // Given: Repository 返回 Optional 包含对象
        MaintainerPo po = buildMaintainerPo();
        when(maintainerRepository.findById(eq(EXISTING_ID)))
                .thenReturn(Optional.of(po));

        // When
        MaintainerPo result = maintainerDao.findById(EXISTING_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(po);
        assertThat(result.getId()).isEqualTo(EXISTING_ID);

        // 验证 Repository 被调用一次
        verify(maintainerRepository, times(1)).findById(EXISTING_ID);
    }

    @Test
    @DisplayName("根据ID查询维修人员 - 不存在时返回 null")
    void findById_shouldReturnNull_whenIdNotExists() {
        // Given: Repository 返回空 Optional
        when(maintainerRepository.findById(eq(NOT_EXIST_ID)))
                .thenReturn(Optional.empty());

        // When
        MaintainerPo result = maintainerDao.findById(NOT_EXIST_ID);

        // Then
        assertThat(result).isNull();

        verify(maintainerRepository, times(1)).findById(NOT_EXIST_ID);
    }
}
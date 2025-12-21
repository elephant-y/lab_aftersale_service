package org.oomall.labservice.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oomall.labservice.dao.MaintainerDao;
import org.oomall.labservice.mapper.Po.MaintainerPo;
import org.oomall.labservice.model.ReturnNo;
import org.oomall.labservice.model.ReturnObject;
import org.oomall.labservice.service.vo.MaintainerResponseVo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)  // 使用 Mockito 的 JUnit 5 扩展
@Slf4j
class MaintainerServiceTest {

    @Mock
    private MaintainerDao maintainerDao;

    @InjectMocks
    private MaintainerService maintainerService;

    private static final Long EXISTING_ID = 100L;
    private static final Long NOT_EXIST_ID = 999L;

    // 构建一个模拟的 MaintainerPo 对象
    private MaintainerPo buildMaintainerPo() {
        MaintainerPo po = new MaintainerPo();
        po.setId(EXISTING_ID);
        po.setMaintainerName("张师傅");
        po.setMaintainerMobile("13800138000");
        // 其他字段根据你的 PO 类实际属性填写
        return po;
    }

    @Test
    @DisplayName("根据ID查询维修人员 - 成功找到，返回 OK 和 VO")
    void findById_shouldReturnOkAndVo_whenMaintainerExists() {
        // Given: DAO 返回一个有效的 MaintainerPo
        MaintainerPo po = buildMaintainerPo();
        when(maintainerDao.findById(eq(EXISTING_ID))).thenReturn(po);

        // When: 调用 Service 方法
        ReturnObject result = maintainerService.findById(EXISTING_ID);

        // Then: 验证返回结果
        assertThat(result).isNotNull();
        assertThat(result.getErrno()).isEqualTo(ReturnNo.OK.getErrNo());
        assertThat(result.getData()).isInstanceOf(MaintainerResponseVo.class);

        MaintainerResponseVo vo = (MaintainerResponseVo) result.getData();
        assertThat(vo.getMaintainer()).isEqualTo(po);

        // 验证日志行为（可选，但推荐验证关键日志）
        // 注意：由于 log 是静态的，Mockito 无法直接 verify，但可以通过捕获或不验证日志来间接确认分支已执行

        // 验证 DAO 被调用一次
        verify(maintainerDao, times(1)).findById(EXISTING_ID);
    }

    @Test
    @DisplayName("根据ID查询维修人员 - 未找到，返回 RESOURCE_ID_NOTEXIST 并记录 error 日志")
    void findById_shouldReturnNotExist_whenMaintainerIsNull() {
        // Given: DAO 返回 null
        when(maintainerDao.findById(eq(NOT_EXIST_ID))).thenReturn(null);

        // When
        ReturnObject result = maintainerService.findById(NOT_EXIST_ID);

        // Then
        assertThat(result.getErrno()).isEqualTo(ReturnNo.RESOURCE_ID_NOTEXIST.getErrNo());
        assertThat(result.getData()).isNull();

        verify(maintainerDao, times(1)).findById(NOT_EXIST_ID);
        // log.error 会被触发 → 分支覆盖
    }

    @Test
    @DisplayName("根据ID查询维修人员 - DAO 抛出异常，返回 RESOURCE_ID_NOTEXIST 并记录异常日志")
    void findById_shouldReturnNotExist_whenExceptionThrown() {
        // Given: DAO 抛出异常
        RuntimeException exception = new RuntimeException("数据库连接失败");
        when(maintainerDao.findById(eq(EXISTING_ID))).thenThrow(exception);

        // When
        ReturnObject result = maintainerService.findById(EXISTING_ID);

        // Then
        assertThat(result.getErrno()).isEqualTo(ReturnNo.RESOURCE_ID_NOTEXIST.getErrNo());
        assertThat(result.getData()).isNull();

        verify(maintainerDao, times(1)).findById(EXISTING_ID);
        // catch 块被执行 → log.error(e) 被触发 → 分支覆盖完成
    }
}

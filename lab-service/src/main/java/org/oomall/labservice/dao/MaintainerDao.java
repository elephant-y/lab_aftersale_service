package org.oomall.labservice.dao;

import lombok.RequiredArgsConstructor;
import org.oomall.labservice.mapper.MaintainerRepository;
import org.oomall.labservice.mapper.Po.MaintainerPo;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MaintainerDao {
    private final MaintainerRepository maintainerRepository;

    // 仅实现：按ID查询维修人员
    public MaintainerPo findById(Long id) {
        // orElse(null)：查询不到返回null，和你现有代码风格一致
        return maintainerRepository.findById(id).orElse(null);
    }
}
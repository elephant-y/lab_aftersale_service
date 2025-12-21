package org.oomall.labservice.mapper;

import org.oomall.labservice.mapper.Po.MaintainerPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintainerRepository extends JpaRepository<MaintainerPo, Long> {

}
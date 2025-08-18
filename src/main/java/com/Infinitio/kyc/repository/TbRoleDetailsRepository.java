package com.Infinitio.kyc.repository;

import com.Infinitio.kyc.entity.TbRoleDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TbRoleDetailsRepository extends JpaRepository<TbRoleDetails, Integer> {

    List<TbRoleDetails> findByRoleId(Integer roleId);

    Optional<TbRoleDetails> findByRoleIdAndFormId(Integer roleId, Integer formId);

}

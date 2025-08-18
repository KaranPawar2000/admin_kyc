package com.Infinitio.kyc.repository;

import com.Infinitio.kyc.entity.TbAdminMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TbAdminMasterRepository extends JpaRepository<TbAdminMaster, Integer> {

    Optional<TbAdminMaster> findByEmailIdAndPassword(String emailId, String password);

}

package com.Infinitio.kyc.repository;

import com.Infinitio.kyc.entity.TbClientMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TbClientMasterRepository extends JpaRepository<TbClientMaster, Integer> {
    TbClientMaster findByApiKey(String apiKey);
    Optional<TbClientMaster> findByEmailIdAndPassword(String emailId, String password);
}
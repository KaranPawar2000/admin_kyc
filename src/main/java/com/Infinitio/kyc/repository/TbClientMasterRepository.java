package com.Infinitio.kyc.repository;

import com.Infinitio.kyc.entity.TbClientMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TbClientMasterRepository extends JpaRepository<TbClientMaster, Integer> {
    TbClientMaster findByApiKey(String apiKey);
}
package com.Infinitio.kyc.repository;

import com.Infinitio.kyc.entity.TbApiTypeRouteMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TbApiTypeRouteMappingRepository extends JpaRepository<TbApiTypeRouteMapping, Integer> {
    List<TbApiTypeRouteMapping> findByClientUserId(Integer clientUserId);

    Optional<TbApiTypeRouteMapping> findByApiTypeId(Integer apiTypeId);

}

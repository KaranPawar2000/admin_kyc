package com.Infinitio.kyc.repository;


import com.Infinitio.kyc.dto.ClientApiUsageCount;
import com.Infinitio.kyc.entity.TbUsageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TbUsageHistoryRepository extends JpaRepository<TbUsageHistory, Integer> {

    @Query(value = "SELECT c.id as clientId, c.org_name as clientName, " +
            "a.name as apiName, COUNT(u.id) as count " +
            "FROM tb_client_master c " +
            "CROSS JOIN tb_api_type_master a " +
            "LEFT JOIN tb_usage_history u ON u.client_id = c.id " +
            "AND u.api_type_id = a.id " +
            "GROUP BY c.id, c.org_name, a.name",
            nativeQuery = true)
    List<Object[]> getClientWiseApiUsageCount();


    @Query(value = "SELECT c.id as clientId, c.org_name as clientName, " +
            "a.name as apiName, COUNT(u.id) as count " +
            "FROM tb_client_master c " +
            "CROSS JOIN tb_api_type_master a " +
            "LEFT JOIN tb_usage_history u ON u.client_id = c.id " +
            "AND u.api_type_id = a.id " +
            "AND u.sent_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
            "WHERE c.id = :clientId " +
                    "GROUP BY c.id, c.org_name, a.name",
            nativeQuery = true)
    List<Object[]> getClientWiseApiUsageCountByIdLast30Days(@Param("clientId") Integer clientId);


    @Query(value = "SELECT c.id as clientId, c.org_name as clientName, " +
            "a.name as apiName, COUNT(u.id) as count " +
            "FROM tb_client_master c " +
            "CROSS JOIN tb_api_type_master a " +
            "LEFT JOIN tb_usage_history u ON u.client_id = c.id " +
            "AND u.api_type_id = a.id " +
            "WHERE c.id = :clientId " +
            "GROUP BY c.id, c.org_name, a.name",
            nativeQuery = true)
    List<Object[]> getClientWiseApiUsageCountById(Integer clientId);

    List<TbUsageHistory> findByClientId(Integer clientId);

}


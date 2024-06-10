package ru.alfastudents.smartmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.alfastudents.smartmatch.entity.AutoAssignCase;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AutoAssignCaseRepository extends JpaRepository<AutoAssignCase, UUID> {

    @Query("SELECT DISTINCT c.managerId FROM AutoAssignCase c WHERE FUNCTION('DATE', c.createdAt)=current_date")
    List<String> findManagerIdByCreatedAtToday();

    @Query("SELECT DISTINCT c.clientId FROM AutoAssignCase c WHERE c.managerId=:managerId AND FUNCTION('DATE', c.createdAt)=current_date")
    List<String> findClientIdByManagerIdAndCreatedAtToday(@Param("managerId") String managerId);

    @Query("SELECT count(*) FROM AutoAssignCase c WHERE c.managerId=:managerId AND FUNCTION('DATE', c.createdAt)=current_date AND c.errorInfo is null")
    Integer countSuccesfullByManagerIdAndCreatedAtToday(@Param("managerId") String managerId);

    Optional<AutoAssignCase> findFirstByClientId(String clientId);
}

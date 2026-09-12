package com.smit.compliq.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smit.compliq.entity.User;
import com.smit.compliq.entity.UserMemory;

@Repository
public interface UserMemoryRepository extends JpaRepository<UserMemory, Long> {

    List<UserMemory> findByUserOrderByUpdatedAtDesc(User user);

    @Query("SELECT m FROM UserMemory m WHERE m.user = :user AND " +
           "(LOWER(m.memoryKey) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.memoryValue) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<UserMemory> searchByKeyword(@Param("user") User user, @Param("keyword") String keyword);

    long countByUser(User user);

    @Modifying
    @Query("DELETE FROM UserMemory m WHERE m.memoryId = " +
           "(SELECT m2.memoryId FROM UserMemory m2 WHERE m2.user = :user ORDER BY m2.updatedAt ASC LIMIT 1)")
    void deleteOldestByUser(@Param("user") User user);

    java.util.Optional<UserMemory> findByUserAndMemoryKey(User user, String memoryKey);
}

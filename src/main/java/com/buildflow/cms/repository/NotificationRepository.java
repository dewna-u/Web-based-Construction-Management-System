package com.buildflow.cms.repository;

import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(AppUser user);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserAndReadFlagFalseOrderByCreatedAtDesc(AppUser user);

    long countByUserAndReadFlagFalse(AppUser user);

    long countByUserIdAndReadFlagFalse(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Notification n SET n.readFlag = true WHERE n.user.id = :userId AND n.readFlag = false")
    int markAllReadForUser(@Param("userId") Long userId);
}

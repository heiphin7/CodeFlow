package com.api.codeflow.repository;

import com.api.codeflow.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastSeen = :time WHERE u.username = :username")
    void updateLastSeen(@Param("username") String username, @Param("time") Date time);
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.submissions WHERE u.username = :username")
    Optional<User> findByUsernameWithSubmissions(@Param("username") String username);

}

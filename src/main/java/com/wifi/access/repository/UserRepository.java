package com.wifi.access.repository;

import com.wifi.access.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByMacAddress(String macAddress);

    Optional<User> findByVkUserId(Long vkUserId);

    List<User> findByIsSubscribedFalse();

    List<User> findByIsSubscribedTrue();

    List<User> findAll();
}


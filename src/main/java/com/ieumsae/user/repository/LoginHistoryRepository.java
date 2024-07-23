package com.ieumsae.user.repository;

import com.ieumsae.user.domain.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
}

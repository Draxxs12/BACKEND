package com.ferreteria.v1.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import com.ferreteria.v1.models.AuthChallenge;

public interface AuthChallengeRepository extends JpaRepository<AuthChallenge, Long> {
    Optional<AuthChallenge> findByTokenAndUsedFalse(String token);
    @Transactional
    void deleteByEmailAndType(String email, AuthChallenge.Type type);
}

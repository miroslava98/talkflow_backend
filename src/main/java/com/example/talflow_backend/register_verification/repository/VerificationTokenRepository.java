package com.example.talflow_backend.register_verification.repository;

import com.example.talflow_backend.register_verification.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {


    VerificationToken findByToken(String token);

    VerificationToken save(VerificationToken token);

    void delete(VerificationToken token);

}

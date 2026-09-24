package com.ferreteria.v1.models;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "auth_challenges", indexes = {
        @Index(name = "idx_auth_challenge_token", columnList = "token"),
        @Index(name = "idx_auth_challenge_email_type", columnList = "email,type")
})
public class AuthChallenge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 80) private String token;
    @Column(nullable = false, length = 150) private String email;
    @Column(nullable = false, length = 64) private String codeHash;
    @Transient private String plainCode;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Type type;
    @Column(nullable = false) private LocalDateTime expiresAt;
    @Column(nullable = false) private int attempts = 0;
    @Column(nullable = false) private boolean used = false;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();

    public enum Type { LOGIN_MFA, PASSWORD_RESET }
    public Long getId(){return id;}
    public String getToken(){return token;} public void setToken(String v){token=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getCodeHash(){return codeHash;} public void setCodeHash(String v){codeHash=v;}
    public String getPlainCode(){return plainCode;} public void setPlainCode(String v){plainCode=v;}
    public Type getType(){return type;} public void setType(Type v){type=v;}
    public LocalDateTime getExpiresAt(){return expiresAt;} public void setExpiresAt(LocalDateTime v){expiresAt=v;}
    public int getAttempts(){return attempts;} public void setAttempts(int v){attempts=v;}
    public boolean isUsed(){return used;} public void setUsed(boolean v){used=v;}
    public LocalDateTime getCreatedAt(){return createdAt;}
}

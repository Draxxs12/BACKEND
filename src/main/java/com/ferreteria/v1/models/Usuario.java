package com.ferreteria.v1.models;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @ManyToOne
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "registrado_por", length = 100)
    private String registradoPor;

    // ── Getters y Setters ──────────────────────────────

    public String  getRegistradoPor()                  { return registradoPor; }
    public void    setRegistradoPor(String r)          { this.registradoPor = r; }

    public Integer getId()                          { return id; }
    public void    setId(Integer id)                { this.id = id; }

    public String  getNombre()                      { return nombre; }
    public void    setNombre(String nombre)         { this.nombre = nombre; }

    public String  getEmail()                       { return email; }
    public void    setEmail(String email)           { this.email = email; }

    public String  getPasswordHash()                       { return passwordHash; }
    public void    setPasswordHash(String passwordHash)    { this.passwordHash = passwordHash; }

    public Rol     getRol()                         { return rol; }
    public void    setRol(Rol rol)                  { this.rol = rol; }

    public Boolean getActivo()                      { return activo; }
    public void    setActivo(Boolean activo)        { this.activo = activo; }

    public LocalDateTime getCreatedAt()                        { return createdAt; }
    public void          setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
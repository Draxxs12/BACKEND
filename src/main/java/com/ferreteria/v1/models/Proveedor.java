package com.ferreteria.v1.models;
 
import java.time.LocalDateTime;
 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
 
@Entity
@Table(name = "proveedores")
public class Proveedor {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
 
    @Column(nullable = false, length = 200)
    private String empresa;
 
    @Column(length = 20)
    private String ruc;
 
    @Column(length = 100)
    private String contacto;
 
    @Column(length = 20)
    private String telefono;
 
    @Column(length = 150)
    private String email;
 
    @Column(length = 255)
    private String direccion;
 
    @Column(nullable = false)
    private Boolean activo = true;
 
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "registrado_por", length = 100)
    private String registradoPor;

    public String getRegistradoPor()              { return registradoPor; }
    public void   setRegistradoPor(String r)      { this.registradoPor = r; }


    public Integer getId()                    { return id; }
    public void    setId(Integer id)          { this.id = id; }
 
    public String  getEmpresa()               { return empresa; }
    public void    setEmpresa(String empresa) { this.empresa = empresa; }
 
    public String  getRuc()                   { return ruc; }
    public void    setRuc(String ruc)         { this.ruc = ruc; }
 
    public String  getContacto()                  { return contacto; }
    public void    setContacto(String contacto)   { this.contacto = contacto; }
 
    public String  getTelefono()                  { return telefono; }
    public void    setTelefono(String telefono)   { this.telefono = telefono; }
 
    public String  getEmail()                 { return email; }
    public void    setEmail(String email)     { this.email = email; }
 
    public String  getDireccion()                   { return direccion; }
    public void    setDireccion(String direccion)   { this.direccion = direccion; }
 
    public Boolean getActivo()                { return activo; }
    public void    setActivo(Boolean activo)  { this.activo = activo; }
 
    public LocalDateTime getCreatedAt()                       { return createdAt; }
    public void          setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
}
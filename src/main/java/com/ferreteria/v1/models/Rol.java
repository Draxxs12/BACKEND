package com.ferreteria.v1.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;   // Administrador | Recepcionista | Almacenero

    public Integer getId()                  { return id; }
    public void    setId(Integer id)        { this.id = id; }

    public String  getNombre()              { return nombre; }
    public void    setNombre(String nombre) { this.nombre = nombre; }
}
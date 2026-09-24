package com.ferreteria.v1.controllers;

/** Cuerpo para crear/actualizar usuarios (la contraseña llega en texto plano y se cifra en el backend). */
public class UsuarioRequest {
    private String  nombre;
    private String  email;
    private String  password;   // opcional en edición
    private Integer rolId;
    private Boolean activo;

    public String  getNombre()             { return nombre; }
    public void    setNombre(String n)     { this.nombre = n; }

    public String  getEmail()              { return email; }
    public void    setEmail(String e)      { this.email = e; }

    public String  getPassword()           { return password; }
    public void    setPassword(String p)   { this.password = p; }

    public Integer getRolId()              { return rolId; }
    public void    setRolId(Integer r)     { this.rolId = r; }

    public Boolean getActivo()             { return activo; }
    public void    setActivo(Boolean a)    { this.activo = a; }
}

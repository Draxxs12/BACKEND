package com.ferreteria.v1.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "contador_comprobante")
public class ContadorComprobante {

    @Id
    @Column(length = 10, nullable = false)
    private String serie;

    @Column(name = "ultimo_numero", nullable = false)
    private Long ultimoNumero = 0L;

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }
    public Long getUltimoNumero() { return ultimoNumero; }
    public void setUltimoNumero(Long ultimoNumero) { this.ultimoNumero = ultimoNumero; }
}

package com.ferreteria.v1.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ferreteria.v1.exceptions.ValidacionException;
import com.ferreteria.v1.exceptions.Validador;
import com.ferreteria.v1.models.Proveedor;
import com.ferreteria.v1.repositories.ProveedorRepository;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private UsuarioActualService usuarioActual;

    public List<Proveedor> listarTodos() {
        return proveedorRepository.findAll();
    }

    public Proveedor buscarPorId(Integer id) {
        return proveedorRepository.findById(id).orElse(null);
    }

    public List<Proveedor> buscarPorEmpresa(String empresa) {
        return proveedorRepository.findByEmpresaContainingIgnoreCase(empresa);
    }

    public Proveedor guardar(Proveedor proveedor) {
        if (proveedor == null) {
            throw new ValidacionException("Debes proporcionar los datos del proveedor.");
        }

        if (proveedor.getEmpresa() == null || proveedor.getEmpresa().isBlank()) {
            throw new ValidacionException("El nombre de la empresa es obligatorio.");
        }

        if (proveedor.getRuc() != null && !proveedor.getRuc().isBlank()
                && !proveedor.getRuc().matches("\\d{11}")) {
            throw new ValidacionException("El RUC debe tener exactamente 11 dígitos numéricos.");
        }

        Validador.email(proveedor.getEmail());
        proveedor.setRegistradoPor(usuarioActual.nombreActual());
        return proveedorRepository.save(proveedor);
    }

    public void eliminar(Integer id) {
        proveedorRepository.findById(id).ifPresent(proveedor -> {
            proveedor.setActivo(false);
            proveedorRepository.save(proveedor);
        });
    }
}

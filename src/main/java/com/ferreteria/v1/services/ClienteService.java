package com.ferreteria.v1.services;
 
import com.ferreteria.v1.exceptions.Validador;
import com.ferreteria.v1.models.Cliente;
import com.ferreteria.v1.repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
 
@Service
public class ClienteService {
 
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioActualService usuarioActual;

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }
 
    public Cliente buscarPorId(Integer id) {
        return clienteRepository.findById(id).orElse(null);
    }
 
    public List<Cliente> buscarPorNombre(String nombre) {
        return clienteRepository.findByNombreContainingIgnoreCase(nombre);
    }
 
    public Cliente guardar(Cliente cliente) {
        Validador.documento(
            cliente.getTipoDocumento() != null ? cliente.getTipoDocumento().name() : null,
            cliente.getNumeroDocumento());
        Validador.email(cliente.getEmail());
        cliente.setRegistradoPor(usuarioActual.nombreActual());
        return clienteRepository.save(cliente);
    }
 
    public void eliminar(Integer id) {
        clienteRepository.deleteById(id);
    }
}
package com.ferreteria.v1.repositories;
 
import com.ferreteria.v1.models.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
}

package com.cafeorigen.repository;

import com.cafeorigen.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ICategoriaRepository extends JpaRepository<Categoria, Integer> {
    List<Categoria> findByEstadoOrderByIdCategoriaDesc(Integer estado);
}

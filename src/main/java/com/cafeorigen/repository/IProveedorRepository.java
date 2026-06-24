package com.cafeorigen.repository;

import com.cafeorigen.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IProveedorRepository extends JpaRepository<Proveedor, Integer> {
    List<Proveedor> findByEstadoOrderByIdProveedorDesc(Integer estado);
}

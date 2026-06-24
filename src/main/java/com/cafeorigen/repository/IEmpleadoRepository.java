package com.cafeorigen.repository;

import com.cafeorigen.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IEmpleadoRepository extends JpaRepository<Empleado, Integer> {
    Optional<Empleado> findByUsuarioIdUsuario(Integer idUsuario);

    List<Empleado> findByEstadoOrderByIdEmpleadoDesc(Integer estado);
}

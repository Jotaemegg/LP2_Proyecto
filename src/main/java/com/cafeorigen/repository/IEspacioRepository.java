package com.cafeorigen.repository;

import com.cafeorigen.model.Espacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IEspacioRepository extends JpaRepository<Espacio, Integer> {
    List<Espacio> findByEstado(String estado);
    List<Espacio> findByTipoAndEstado(String tipo, String estado);
    List<Espacio> findByEstadoNot(String estado);
}

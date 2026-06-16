package com.cafeorigen.repository;

import com.cafeorigen.model.ReservaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IReservaDetalleRepository extends JpaRepository<ReservaDetalle, Integer> {
    List<ReservaDetalle> findByReservaIdReserva(Integer idReserva);
    List<ReservaDetalle> findByEspacioIdEspacioAndFechaReserva(Integer idEspacio, java.time.LocalDate fechaReserva);
}

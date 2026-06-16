package com.cafeorigen.repository;

import com.cafeorigen.model.ConsumoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IConsumoDetalleRepository extends JpaRepository<ConsumoDetalle, Integer> {
    List<ConsumoDetalle> findByReservaIdReserva(Integer idReserva);
}

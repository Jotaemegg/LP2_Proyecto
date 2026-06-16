package com.cafeorigen.repository;

import com.cafeorigen.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IReservaRepository extends JpaRepository<Reserva, Integer> {
    List<Reserva> findByClienteIdCliente(Integer idCliente);
}

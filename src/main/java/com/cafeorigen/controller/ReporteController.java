package com.cafeorigen.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import com.cafeorigen.model.Usuario;
import com.cafeorigen.model.Reserva;
import com.cafeorigen.repository.IReservaRepository;
import com.cafeorigen.repository.IClienteRepository;

import net.sf.jasperreports.engine.*;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private IReservaRepository reservaRepository;

    @Autowired
    private IClienteRepository clienteRepository;

    private boolean validarSesionYRol(HttpSession session, String... rolesValidos) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return false;
        
        String rolUsuario = usuario.getRol().getNombre();
        for (String rol : rolesValidos) {
            if (rol.equalsIgnoreCase(rolUsuario)) {
                return true;
            }
        }
        return false;
    }

    @GetMapping("/boleta/{id}")
    public void exportarBoleta(@PathVariable("id") Integer idReserva, HttpSession session, HttpServletResponse response) {
        if (!validarSesionYRol(session, "ADMINISTRADOR", "RECEPCIONISTA", "CLIENTE")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario.getRol().getNombre().equalsIgnoreCase("CLIENTE")) {
            Reserva reserva = reservaRepository.findById(idReserva).orElse(null);
            if (reserva == null || reserva.getCliente() == null || 
                reserva.getCliente().getUsuario() == null || 
                !reserva.getCliente().getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "inline; filename=Boleta_Reserva_" + idReserva + ".pdf");

        try (Connection conn = dataSource.getConnection()) {
            InputStream reportStream = this.getClass().getResourceAsStream("/reportes/BoletaReserva.jasper");
            
            if (reportStream == null) {
                InputStream jrxmlStream = this.getClass().getResourceAsStream("/reportes/BoletaReserva.jrxml");
                if (jrxmlStream == null) {
                    throw new RuntimeException("No se encontró la plantilla de la boleta (.jrxml o .jasper).");
                }
                JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("ID_RESERVA", idReserva);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
                OutputStream out = response.getOutputStream();
                JasperExportManager.exportReportToPdfStream(jasperPrint, out);
            } else {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("ID_RESERVA", idReserva);
                JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, parameters, conn);
                OutputStream out = response.getOutputStream();
                JasperExportManager.exportReportToPdfStream(jasperPrint, out);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar el reporte de la boleta: " + e.getMessage());
        }
    }

    @GetMapping("/dashboard")
    public void exportarDashboardVentas(HttpSession session, HttpServletResponse response) {
        if (!validarSesionYRol(session, "ADMINISTRADOR", "RECEPCIONISTA")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "inline; filename=Dashboard_Ventas_CafeOrigen.pdf");

        try (Connection conn = dataSource.getConnection()) {
            InputStream reportStream = this.getClass().getResourceAsStream("/reportes/DashboardVentas.jasper");
            
            if (reportStream == null) {
                InputStream jrxmlStream = this.getClass().getResourceAsStream("/reportes/DashboardVentas.jrxml");
                if (jrxmlStream == null) {
                    throw new RuntimeException("No se encontró la plantilla del dashboard (.jrxml o .jasper).");
                }
                JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap<>(), conn);
                OutputStream out = response.getOutputStream();
                JasperExportManager.exportReportToPdfStream(jasperPrint, out);
            } else {
                JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, new HashMap<>(), conn);
                OutputStream out = response.getOutputStream();
                JasperExportManager.exportReportToPdfStream(jasperPrint, out);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar el reporte de ventas consolidado: " + e.getMessage());
        }
    }
}

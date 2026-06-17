package com.cafeorigen.controller;

import com.cafeorigen.model.Cliente;
import com.cafeorigen.model.Reserva;
import com.cafeorigen.model.Usuario;
import com.cafeorigen.repository.IReservaRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private static final String BOLETA_JRXML = "reportes/BoletaReserva.jrxml";
    private static final String DASHBOARD_JRXML = "reportes/DashboardVentas.jrxml";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private IReservaRepository reservaRepository;

    private boolean validarSesionYRol(HttpSession session, String... rolesValidos) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return false;
        }

        String rolUsuario = usuario.getRol().getNombre();
        for (String rol : rolesValidos) {
            if (rol.equalsIgnoreCase(rolUsuario)) {
                return true;
            }
        }
        return false;
    }

    @GetMapping("/boleta/{id}")
    @Transactional(readOnly = true)
    public void exportarBoleta(@PathVariable("id") Integer idReserva,
                               HttpSession session,
                               HttpServletResponse response) throws Exception {
        if (!validarSesionYRol(session, "ADMINISTRADOR", "RECEPCIONISTA", "CLIENTE")) {
            response.sendRedirect("/login");
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        Reserva reserva = reservaRepository.findById(idReserva).orElse(null);
        if (reserva == null) {
            response.sendRedirect("/reservas");
            return;
        }

        if ("CLIENTE".equalsIgnoreCase(usuario.getRol().getNombre())) {
            Cliente cliente = (Cliente) session.getAttribute("clienteLogueado");
            if (cliente == null || reserva.getCliente() == null
                    || !reserva.getCliente().getIdCliente().equals(cliente.getIdCliente())) {
                response.sendRedirect("/reservas");
                return;
            }
        }

        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ID_RESERVA", idReserva);

            byte[] pdfBytes = generarPdf(BOLETA_JRXML, parameters);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=Boleta_Reserva_" + idReserva + ".pdf");
            response.setContentLength(pdfBytes.length);
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            response.sendRedirect("/reservas/ver/" + idReserva + "?error=pdf");
        }
    }

    @GetMapping("/dashboard")
    public void exportarDashboardVentas(HttpSession session, HttpServletResponse response) throws Exception {
        if (!validarSesionYRol(session, "ADMINISTRADOR", "RECEPCIONISTA")) {
            response.sendRedirect("/login");
            return;
        }

        try {
            byte[] pdfBytes = generarPdf(DASHBOARD_JRXML, new HashMap<>());

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=Dashboard_Ventas_CafeOrigen.pdf");
            response.setContentLength(pdfBytes.length);
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            response.sendRedirect("/dashboard?error=pdf");
        }
    }

    private byte[] generarPdf(String jrxmlPath, Map<String, Object> parameters) throws Exception {
        ClassPathResource resource = new ClassPathResource(jrxmlPath);
        if (!resource.exists()) {
            throw new IllegalStateException("No se encontró la plantilla del reporte: " + jrxmlPath);
        }

        try (Connection conn = dataSource.getConnection();
             InputStream jrxmlStream = resource.getInputStream();
             ByteArrayOutputStream pdfStream = new ByteArrayOutputStream()) {

            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
            JasperExportManager.exportReportToPdfStream(jasperPrint, pdfStream);
            return pdfStream.toByteArray();
        }
    }
}
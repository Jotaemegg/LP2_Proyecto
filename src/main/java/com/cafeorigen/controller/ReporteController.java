package com.cafeorigen.controller;

import com.cafeorigen.model.Usuario;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private static final String PRODUCTOS_JASPER = "reportes/ReporteProductos.jasper";
    private static final String EMPLEADOS_JASPER = "reportes/ReporteEmpleados.jasper";

    @Autowired
    private DataSource dataSource;

    private boolean sesionActiva(HttpSession session) {
        return session.getAttribute("usuarioLogueado") != null;
    }

    @GetMapping("/productos")
    @Transactional(readOnly = true)
    public void reporteProductos(HttpSession session, HttpServletResponse response) throws Exception {
        if (!sesionActiva(session)) {
            response.sendRedirect("/login");
            return;
        }
        exportar(PRODUCTOS_JASPER, "Reporte_Productos_CafeOrigen.pdf", response);
    }

    @GetMapping("/empleados")
    @Transactional(readOnly = true)
    public void reporteEmpleados(HttpSession session, HttpServletResponse response) throws Exception {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"ADMINISTRADOR".equalsIgnoreCase(usuario.getRol().getNombre())) {
            response.sendRedirect("/dashboard");
            return;
        }
        exportar(EMPLEADOS_JASPER, "Reporte_Empleados_CafeOrigen.pdf", response);
    }

    private void exportar(String jasperPath, String fileName, HttpServletResponse response) {
        try {
            ClassPathResource resource = new ClassPathResource(jasperPath);
            try (Connection conn = dataSource.getConnection();
                 InputStream jasperStream = resource.getInputStream();
                 ByteArrayOutputStream pdfStream = new ByteArrayOutputStream()) {

                JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap<>(), conn);
                JasperExportManager.exportReportToPdfStream(jasperPrint, pdfStream);

                byte[] pdfBytes = pdfStream.toByteArray();
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "inline; filename=" + fileName);
                response.setContentLength(pdfBytes.length);
                response.getOutputStream().write(pdfBytes);
                response.getOutputStream().flush();
            }
        } catch (Exception e) {
            try {
                response.sendRedirect("/dashboard?error=pdf");
            } catch (Exception ignored) {
            }
        }
    }
}

package com.backend.depahub.services;

import com.backend.depahub.models.Administrador;
import com.backend.depahub.models.Contrato;
import com.backend.depahub.models.Inmueble;
import com.backend.depahub.models.Inquilino;
import com.backend.depahub.models.Propiedad;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContratoPdfServiceTest {

    @Test
    void generaUnPdfDeUnaPaginaConLosDatosDelContrato() throws Exception {
        ContratoPdfService service = new ContratoPdfService();

        byte[] pdf = service.generar(contratoDePrueba());

        assertTrue(new String(pdf, 0, 4).startsWith("%PDF"));

        PdfReader reader = new PdfReader(pdf);
        assertTrue(reader.getNumberOfPages() >= 1);

        String contenido = textoDelPdf(reader);
        assertTrue(contenido.contains("FRECUENCIA DE PAGO"));
        assertTrue(contenido.contains("NUMERO DE CUOTAS"));
        assertTrue(contenido.contains("Ana Perez"));
        assertTrue(contenido.contains("12345678"));
        assertTrue(contenido.contains("Luis Gomez"));
        assertTrue(contenido.contains("87654321"));
        assertTrue(contenido.contains("Departamento 401"));
        assertTrue(contenido.contains("Av. Principal 123"));
        assertTrue(contenido.contains("Miraflores"));
        assertTrue(contenido.contains("CUARTA. CONDICIONES PARTICULARES"));
        assertTrue(contenido.contains("El pago se realiza el primer dia de cada mes."));
    }

    @Test
    void generaElPdfAunqueNoHayaCondicionesParticulares() throws Exception {
        ContratoPdfService service = new ContratoPdfService();
        Contrato contrato = contratoDePrueba();
        contrato.setCondiciones(null);

        byte[] pdf = service.generar(contrato);

        assertTrue(new String(pdf, 0, 4).startsWith("%PDF"));
        String contenido = textoDelPdf(new PdfReader(pdf));
        assertTrue(contenido.contains("QUINTA. CONFORMIDAD"));
        assertFalse(contenido.contains("CUARTA. CONDICIONES PARTICULARES"));
    }

    private String textoDelPdf(PdfReader reader) throws Exception {
        StringBuilder contenido = new StringBuilder();
        PdfTextExtractor extractor = new PdfTextExtractor(reader);
        for (int pagina = 1; pagina <= reader.getNumberOfPages(); pagina++) {
            contenido.append(extractor.getTextFromPage(pagina));
        }
        return contenido.toString();
    }

    private Contrato contratoDePrueba() {
        Administrador administrador = new Administrador();
        administrador.setId(1L);
        administrador.setNombreCompleto("Ana Perez");
        administrador.setDni("12345678");
        administrador.setEmail("ana@depahub.test");

        Propiedad propiedad = new Propiedad();
        propiedad.setNombre("Edificio Central");
        propiedad.setDireccion("Av. Principal 123");
        propiedad.setDistrito("Miraflores");

        Inmueble inmueble = new Inmueble();
        inmueble.setNombre("Departamento 401");
        inmueble.setPropiedad(propiedad);

        Inquilino inquilino = new Inquilino();
        inquilino.setNombreCompleto("Luis Gomez");
        inquilino.setDni("87654321");
        inquilino.setInmueble(inmueble);

        Contrato contrato = new Contrato();
        contrato.setId(9L);
        contrato.setAdministrador(administrador);
        contrato.setInquilino(inquilino);
        contrato.setFechaInicio(LocalDate.of(2026, 10, 1));
        contrato.setFechaFin(LocalDate.of(2027, 9, 30));
        contrato.setFechaRegistro(LocalDate.of(2026, 9, 29));
        contrato.setMontoAlquiler(new BigDecimal("1500.00"));
        contrato.setGarantia(new BigDecimal("1500.00"));
        contrato.setFrecuencia(1);
        contrato.setNumeroCuotas(12);
        contrato.setCondiciones("El pago se realiza el primer dia de cada mes.");
        return contrato;
    }
}

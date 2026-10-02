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

import static org.junit.jupiter.api.Assertions.assertTrue;

class ContratoPdfServiceTest {

    @Test
    void generaUnPdfDeUnaPaginaConLosDatosDelContrato() throws Exception {
        ContratoPdfService service = new ContratoPdfService();

        byte[] pdf = service.generar(contratoDePrueba());

        assertTrue(new String(pdf, 0, 4).startsWith("%PDF"));

        PdfReader reader = new PdfReader(pdf);
        assertTrue(reader.getNumberOfPages() >= 1);

        StringBuilder contenido = new StringBuilder();
        PdfTextExtractor extractor = new PdfTextExtractor(reader);
        for (int pagina = 1; pagina <= reader.getNumberOfPages(); pagina++) {
            contenido.append(extractor.getTextFromPage(pagina));
        }
        assertTrue(contenido.toString().contains("FRECUENCIA DE PAGO"));
        assertTrue(contenido.toString().contains("NUMERO DE CUOTAS"));
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

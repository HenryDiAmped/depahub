package com.backend.depahub.services;

import com.backend.depahub.models.Contrato;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class ContratoPdfService {

    private static final Color NAVY = new Color(29, 40, 72);
    private static final Color CORAL = new Color(255, 112, 102);
    private static final Color LIGHT_GRAY = new Color(245, 247, 250);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' uuuu", Locale.forLanguageTag("es-PE"));

    public byte[] generar(Contrato contrato) {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 54, 54, 58, 54);
            PdfWriter.getInstance(document, salida);
            document.open();

            agregarTitulo(document, contrato);
            agregarResumen(document, contrato);
            agregarClausulas(document, contrato);
            agregarFirmas(document, contrato);

            document.close();
            return salida.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo generar el PDF del contrato", exception);
        }
    }

    private void agregarTitulo(Document document, Contrato contrato) throws Exception {
        Paragraph titulo = new Paragraph("CONTRATO DE ARRENDAMIENTO", fuente(18, Font.BOLD, NAVY));
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(7);
        document.add(titulo);

        Paragraph subtitulo = new Paragraph("Contrato N. " + contrato.getId(), fuente(10, Font.NORMAL, CORAL));
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(22);
        document.add(subtitulo);
    }

    private void agregarResumen(Document document, Contrato contrato) throws Exception {
        PdfPTable tabla = new PdfPTable(new float[]{1.15f, 1.85f});
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(20);

        agregarFila(tabla, "ARRENDADOR", contrato.getAdministrador().getNombreCompleto());
        agregarFila(tabla, "DNI DEL ARRENDADOR", contrato.getAdministrador().getDni());
        agregarFila(tabla, "ARRENDATARIO", contrato.getInquilino().getNombreCompleto());
        agregarFila(tabla, "DNI DEL ARRENDATARIO", contrato.getInquilino().getDni());
        agregarFila(tabla, "INMUEBLE", contrato.getInquilino().getInmueble().getNombre());
        agregarFila(tabla, "UBICACION", ubicacion(contrato));
        agregarFila(tabla, "VIGENCIA", fecha(contrato.getFechaInicio()) + " al " + fecha(contrato.getFechaFin()));
        agregarFila(tabla, "ALQUILER MENSUAL", moneda(contrato.getMontoAlquiler()));
        agregarFila(tabla, "GARANTIA", moneda(contrato.getGarantia()));
        agregarFila(tabla, "FRECUENCIA DE PAGO", "Cada " + contrato.getFrecuencia() + " meses");
        agregarFila(tabla, "NUMERO DE CUOTAS", contrato.getNumeroCuotas().toString());

        document.add(tabla);
    }

    private void agregarClausulas(Document document, Contrato contrato) throws Exception {
        agregarSeccion(document, "PRIMERA. OBJETO DEL CONTRATO",
                "El arrendador entrega en arrendamiento al arrendatario el inmueble descrito en este documento, "
                        + "quien lo recibe en las condiciones acordadas para uso de vivienda.");
        agregarSeccion(document, "SEGUNDA. PLAZO",
                "El presente contrato tiene vigencia desde el " + fecha(contrato.getFechaInicio())
                        + " hasta el " + fecha(contrato.getFechaFin()) + ".");
        agregarSeccion(document, "TERCERA. RENTA Y GARANTIA",
                "El arrendatario pagara una renta de " + moneda(contrato.getMontoAlquiler())
                        + " cada " + contrato.getFrecuencia() + " meses, en un total de "
                        + contrato.getNumeroCuotas() + " cuotas. Asimismo, entrega una garantia de " + moneda(contrato.getGarantia())
                        + ", sujeta a las condiciones de devolucion aplicables al finalizar el contrato.");
        if (contrato.getCondiciones() != null && !contrato.getCondiciones().isBlank()) {
            agregarSeccion(document, "CUARTA. CONDICIONES PARTICULARES", contrato.getCondiciones());
        }
        agregarSeccion(document, "QUINTA. CONFORMIDAD",
                "Las partes declaran haber leido y aceptado el contenido del presente contrato. "
                        + "Se firma en dos ejemplares de igual valor.");
    }

    private void agregarFirmas(Document document, Contrato contrato) throws Exception {
        PdfPTable firmas = new PdfPTable(2);
        firmas.setWidthPercentage(100);
        firmas.setSpacingBefore(32);

        agregarFirma(firmas, contrato.getAdministrador().getNombreCompleto(), "ARRENDADOR");
        agregarFirma(firmas, contrato.getInquilino().getNombreCompleto(), "ARRENDATARIO");
        document.add(firmas);

        Paragraph fechaEmision = new Paragraph("Emitido el " + fecha(contrato.getFechaRegistro()) + ".",
                fuente(8, Font.ITALIC, Color.GRAY));
        fechaEmision.setAlignment(Element.ALIGN_CENTER);
        fechaEmision.setSpacingBefore(18);
        document.add(fechaEmision);
    }

    private void agregarFila(PdfPTable tabla, String etiqueta, String valor) {
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, fuente(8, Font.BOLD, NAVY)));
        celdaEtiqueta.setBackgroundColor(LIGHT_GRAY);
        celdaEtiqueta.setPadding(7);
        celdaEtiqueta.setBorderColor(Color.WHITE);
        tabla.addCell(celdaEtiqueta);

        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, fuente(9, Font.NORMAL, Color.DARK_GRAY)));
        celdaValor.setPadding(7);
        celdaValor.setBorderColor(Color.WHITE);
        tabla.addCell(celdaValor);
    }

    private void agregarSeccion(Document document, String titulo, String contenido) throws Exception {
        Paragraph encabezado = new Paragraph(titulo, fuente(10, Font.BOLD, NAVY));
        encabezado.setSpacingBefore(10);
        encabezado.setSpacingAfter(4);
        document.add(encabezado);

        Paragraph parrafo = new Paragraph(contenido, fuente(10, Font.NORMAL, Color.DARK_GRAY));
        parrafo.setAlignment(Element.ALIGN_JUSTIFIED);
        parrafo.setLeading(0, 1.45f);
        document.add(parrafo);
    }

    private void agregarFirma(PdfPTable firmas, String nombre, String rol) {
        PdfPCell celda = new PdfPCell();
        celda.setBorder(PdfPCell.NO_BORDER);
        celda.setPaddingTop(22);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.addElement(parrafoCentrado("_______________________________", 10, Font.NORMAL, Color.DARK_GRAY));
        celda.addElement(parrafoCentrado(nombre, 9, Font.BOLD, NAVY));
        celda.addElement(parrafoCentrado(rol, 8, Font.NORMAL, Color.GRAY));
        firmas.addCell(celda);
    }

    private Paragraph parrafoCentrado(String contenido, float tamanio, int estilo, Color color) {
        Paragraph parrafo = new Paragraph(contenido, fuente(tamanio, estilo, color));
        parrafo.setAlignment(Element.ALIGN_CENTER);
        return parrafo;
    }

    private String ubicacion(Contrato contrato) {
        var propiedad = contrato.getInquilino().getInmueble().getPropiedad();
        return propiedad.getDireccion() + ", " + propiedad.getDistrito();
    }

    private String fecha(LocalDate fecha) {
        return fecha.format(DATE_FORMAT);
    }

    private String moneda(BigDecimal monto) {
        return "S/. " + monto.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private Font fuente(float tamanio, int estilo, Color color) {
        return FontFactory.getFont(FontFactory.HELVETICA, tamanio, estilo, color);
    }
}

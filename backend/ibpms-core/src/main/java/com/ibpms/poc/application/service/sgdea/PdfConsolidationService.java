package com.ibpms.poc.application.service.sgdea;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Motor de Unificación PDF (CA-14).
 * Toma varios streams y los une en un solo archivo PDF Maestro secuencialmente.
 */
@Service
public class PdfConsolidationService {

    /**
     * Une una lista de streams PDF en un único stream de salida temporal.
     * 
     * @param documentStreams Lista de InputStreams PDF (por ejemplo descargados de
     *                        SharePoint)
     * @return InputStream del PDF final unificado
     * @throws IOException Si falla el parseo o la mezcla de páginas
     */
    public InputStream mergePdfs(List<InputStream> documentStreams) throws IOException {
        if (documentStreams == null || documentStreams.isEmpty()) {
            throw new IllegalArgumentException("Se requiere al menos un documento para unificar.");
        }

        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        pdfMerger.setDestinationStream(outputStream);

        for (InputStream is : documentStreams) {
            pdfMerger.addSource(is);
        }

        // Se usa setup de uso de memoria equilibrada (cache on disk if exceeded memory)
        pdfMerger.mergeDocuments(MemoryUsageSetting.setupMixed(1024 * 1024 * 10)); // 10 MB límite en RAM

        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}

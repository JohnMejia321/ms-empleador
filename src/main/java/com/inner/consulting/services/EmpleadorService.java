package com.inner.consulting.services;

import com.inner.consulting.repositories.EmpleadorRepository;
import com.inner.consulting.entities.Empleador;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class EmpleadorService {

    @Autowired
    private EmpleadorRepository empleadorRepository;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private ITesseract tesseract;

    private String minionEndpoint = "http://localhost:9000";
    private String minionBucketName = "my-bucket";

    public Empleador saveEmpleador(String nombre, String apellido, MultipartFile pdfFile) throws Exception {
        try {
            // Generate a unique id for the user
            UUID empleadorId = UUID.randomUUID();

            // Generate a unique name for the pdf file
            String pdfName = empleadorId + "-" + pdfFile.getOriginalFilename();

            // Upload the pdf file to Minion
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minionBucketName)
                            .object(pdfName)
                            .stream(pdfFile.getInputStream(), pdfFile.getSize(), -1)
                            .contentType(pdfFile.getContentType())
                            .build());

            // Generate the pdf url
            String pdfUrl = minionEndpoint + "/" + minionBucketName + "/" + pdfName;

            // Procesar el PDF con Tesseract
            String ocrResult = procesarPDF(pdfFile.getInputStream());

            // Loguear el resultado del OCR (puedes ajustarlo según tus necesidades)
            System.out.println("Texto extraído del PDF: " + ocrResult);

            // Create a new user object
            Empleador empleador = new Empleador(empleadorId, nombre, apellido, pdfUrl);

            // Save the user to Cassandra
            empleadorRepository.save(empleador);

            // Return the user
            return empleador;
        } catch (Exception e) {
            System.err.println("Error al procesar y guardar el empleador: " + e.getMessage());
            throw e;
        }
    }

    private String procesarPDF(InputStream pdfStream) throws TesseractException {
        try {
            // Crear un archivo temporal
            Path tempPdfPath = Files.createTempFile("temp-pdf", ".pdf");

            // Escribir el contenido del InputStream al archivo temporal
            Files.copy(pdfStream, tempPdfPath, StandardCopyOption.REPLACE_EXISTING);

            // Convertir Path a File
            File pdfFile = tempPdfPath.toFile();

            // Realizar OCR con Tesseract
            String ocrResult = tesseract.doOCR(pdfFile);

            // Eliminar el archivo temporal
            Files.delete(tempPdfPath);

            return ocrResult;
        } catch (Exception e) {
            System.err.println("Error al procesar el PDF con Tesseract: " + e.getMessage());
            throw new TesseractException(e.getMessage());
        }
    }
}

package com.inner.consulting.services;

import com.inner.consulting.repositories.EmpleadorRepository;
import com.inner.consulting.entities.Empleador;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class EmpleadorService {

    @Autowired
    private EmpleadorRepository empleadorRepository;

    // @Value("${minion.endpoint}")
    private String minionEndpoint = "http://localhost:9000";

    // @Value("${minion.access-key}")
    // private String minionAccessKey;

    // @Value("${minion.secret-key}")
    // private String minionSecretKey;

    // @Value("${minion.bucket-name}")
    private String minionBucketName = "my-bucket";

    public Empleador saveEmpleador(String nombre, String apellido, MultipartFile pdfFile) throws Exception {
        // Create a MinioClient object
        MinioClient minioClient = MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials("minioadmin", "minioadmin")
                .build();

        // Generate a unique id for the user
        UUID empleadorId = UUID.randomUUID();

        // Generate a unique name for the pdf file
        String pdfName = empleadorId + "-" + pdfFile.getOriginalFilename();

        // Upload the pdf file to Minion
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket("my-bucket")
                        .object(pdfName)
                        .stream(pdfFile.getInputStream(), pdfFile.getSize(), -1)
                        .contentType(pdfFile.getContentType())
                        .build());

        // Generate the pdf url
        String pdfUrl = minionEndpoint + "/" + minionBucketName + "/" + pdfName;

        // Create a new user object
        Empleador empleador = new Empleador(empleadorId, nombre, apellido, pdfUrl);

        // Save the user to Casandra
        empleadorRepository.save(empleador);

        // Return the user
        return empleador;
    }
}

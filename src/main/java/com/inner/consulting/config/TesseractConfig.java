package com.inner.consulting.config;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TesseractConfig {

    @Bean
    public Tesseract getTesseract() {
        Tesseract instance = new Tesseract();
        // Establece la ruta al directorio tessdata
        instance.setDatapath(
                "C:/Users/John-Mejia/Documents/programacion/inner-consulting/microservicios/inscripcion-empleador/tessdata");
        instance.setLanguage("spa");
        return instance;
    }
}

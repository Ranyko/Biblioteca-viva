package br.com.bibliotecaviva.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BibliotecaClockConfig {
    @Bean
    Clock bibliotecaClock(@Value("${app.biblioteca.zone-id:America/Sao_Paulo}") String zoneId) {
        return Clock.system(ZoneId.of(zoneId));
    }
}

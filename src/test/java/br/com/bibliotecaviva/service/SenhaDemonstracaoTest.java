package br.com.bibliotecaviva.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class SenhaDemonstracaoTest {

    @Test
    void senhaDoReadmeDeveConferirComHashDaMigracao() throws Exception {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream(
                "/db/migration/V2__corrige_senha_demonstracao.sql"))) {
            String sql = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            var matcher = Pattern.compile("SET senha_hash = '([^']+)'").matcher(sql);
            assertTrue(matcher.find(), "Migração deve definir a senha de demonstração");
            assertTrue(new BCryptPasswordEncoder().matches("password", matcher.group(1)),
                    "O login de demonstração precisa aceitar a senha publicada no README");
        }
    }
}

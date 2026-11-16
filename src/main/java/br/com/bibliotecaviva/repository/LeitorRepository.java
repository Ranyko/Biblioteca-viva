package br.com.bibliotecaviva.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.bibliotecaviva.model.Leitor;

public interface LeitorRepository extends JpaRepository<Leitor, Long> {

    boolean existsByDocumento(String documento);

    Optional<Leitor> findByUsuarioId(Long usuarioId);
}

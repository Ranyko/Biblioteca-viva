package br.com.bibliotecaviva.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.bibliotecaviva.model.Exemplar;
import br.com.bibliotecaviva.model.StatusExemplar;

public interface ExemplarRepository extends JpaRepository<Exemplar, Long> {

    long countByLivroIdAndStatus(Long livroId, StatusExemplar status);

}
package br.com.bibliotecaviva.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.bibliotecaviva.model.Livro;

public interface LivroRepository extends JpaRepository<Livro, Long> {
}
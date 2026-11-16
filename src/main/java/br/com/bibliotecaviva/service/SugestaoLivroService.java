package br.com.bibliotecaviva.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.bibliotecaviva.dto.SugestaoLivroResponse;
import br.com.bibliotecaviva.repository.SugestaoLivroRepository;

@Service
public class SugestaoLivroService {

    private final SugestaoLivroRepository sugestaoLivroRepository;

    public SugestaoLivroService(
            SugestaoLivroRepository sugestaoLivroRepository) {
        this.sugestaoLivroRepository = sugestaoLivroRepository;
    }

    public List<SugestaoLivroResponse> buscarSugestoes(Long leitorId) {
        return sugestaoLivroRepository.buscarSugestoes(leitorId);
    }
}
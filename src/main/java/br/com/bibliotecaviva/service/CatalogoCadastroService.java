package br.com.bibliotecaviva.service;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.bibliotecaviva.dto.*;
import br.com.bibliotecaviva.exception.ConflitoException;
import br.com.bibliotecaviva.repository.CatalogoCadastroRepository;
import br.com.bibliotecaviva.repository.CatalogoCadastroRepository.Tipo;

@Service
public class CatalogoCadastroService {
    private final CatalogoCadastroRepository repository;
    public CatalogoCadastroService(CatalogoCadastroRepository repository) { this.repository=repository; }

    @Transactional(readOnly=true)
    public List<ReferenciaResponse> listarReferencias(Tipo tipo,int pagina,int tamanho) {
        paginacao(pagina,tamanho); return repository.listarReferencias(tipo,pagina,tamanho);
    }
    @Transactional
    public ReferenciaResponse criarReferencia(Tipo tipo,NomeCadastroRequest r) {
        return repository.criarReferencia(tipo,nome(tipo,r.nome()));
    }
    @Transactional(readOnly=true)
    public ReferenciaResponse buscarReferencia(Tipo tipo,long id) {
        positivo(id); return repository.referencia(tipo,id);
    }
    @Transactional
    public ReferenciaResponse atualizarReferencia(Tipo tipo,long id,NomeCadastroRequest r) {
        positivo(id); return repository.atualizarReferencia(tipo,id,nome(tipo,r.nome()));
    }
    private String nome(Tipo tipo,String valor) {
        String nome=valor.trim();
        if (nome.isEmpty() || nome.length()>(tipo==Tipo.AUTOR?160:80)) throw new IllegalArgumentException("Nome inválido para o cadastro");
        return nome;
    }
    @Transactional(readOnly=true)
    public LivroResponse buscarLivro(long id) { positivo(id); return repository.livro(id,false); }
    @Transactional(readOnly=true)
    public PaginaResponse<LivroResponse> listarLivros(int pagina,int tamanho) {
        paginacao(pagina,tamanho); return repository.listarLivros(pagina,tamanho);
    }
    @Transactional
    public LivroResponse criarLivro(LivroRequest r) {
        String isbn=conferirLivro(r,0);
        long id=repository.criarLivro(r,isbn);
        vinculos(id,r); return repository.livro(id,false);
    }
    @Transactional
    public LivroResponse atualizarLivro(long id,LivroRequest r) {
        positivo(id); repository.livro(id,true);
        String isbn=conferirLivro(r,id);
        repository.atualizarLivro(id,r,isbn);
        vinculos(id,r); return repository.livro(id,false);
    }
    private String conferirLivro(LivroRequest r,long excetoId) {
        String isbn=r.isbn().replaceAll("[-\\s]", "").toUpperCase(Locale.ROOT);
        if (!isbn.matches("(?:[0-9]{13}|[0-9]{9}[0-9X])")) throw new IllegalArgumentException("ISBN deve ter 10 ou 13 caracteres válidos");
        if (repository.isbnOcupado(isbn,excetoId)) throw new ConflitoException("Já existe livro com este ISBN");
        conferirIds(Tipo.AUTOR,r.autorIds()); conferirIds(Tipo.CATEGORIA,r.categoriaIds());
        return isbn;
    }
    private void conferirIds(Tipo tipo,List<Long> ids) {
        if (ids==null || ids.size()>50 || ids.stream().anyMatch(i->i==null || i<=0) || new HashSet<>(ids).size()!=ids.size())
            throw new IllegalArgumentException("Vínculos devem ter IDs positivos, sem repetição, no máximo 50 por lista");
        repository.conferirReferencias(tipo,ids);
    }
    private void vinculos(long id,LivroRequest r) {
        repository.substituirVinculos(id,Tipo.AUTOR,r.autorIds());
        repository.substituirVinculos(id,Tipo.CATEGORIA,r.categoriaIds());
    }
    @Transactional(readOnly=true)
    public ExemplarResponse buscarExemplar(long id) { positivo(id); return repository.exemplar(id,false); }
    @Transactional(readOnly=true)
    public PaginaResponse<ExemplarResponse> listarExemplares(Long livroId,int pagina,int tamanho) {
        paginacao(pagina,tamanho); if (livroId!=null) positivo(livroId);
        return repository.listarExemplares(livroId,pagina,tamanho);
    }
    @Transactional
    public ExemplarResponse criarExemplar(ExemplarCreateRequest r) {
        if (!repository.livro(r.livroId(),true).ativo()) throw new ConflitoException("Livro inativo não recebe novos exemplares");
        String tombo=tombo(r.codigoTombo(),0);
        return repository.criarExemplar(r,tombo);
    }
    @Transactional
    public ExemplarResponse atualizarExemplar(long id,ExemplarUpdateRequest r) {
        travarExemplar(id);
        return repository.atualizarExemplar(id,r,tombo(r.codigoTombo(),id));
    }
    @Transactional
    public ExemplarResponse situacaoExemplar(long id,boolean ativo) {
        var exemplar=travarExemplar(id);
        if (!Set.of("disponivel","inativo").contains(exemplar.status()) || repository.movimentacaoAtiva(id))
            throw new ConflitoException("Exemplar com empréstimo ou reserva não permite alteração manual de situação");
        if (ativo && !repository.livro(exemplar.livroId(),false).ativo())
            throw new ConflitoException("Livro inativo não permite disponibilizar exemplar");
        return repository.situacao(id,ativo);
    }
    private ExemplarResponse travarExemplar(long id) {
        positivo(id);
        var referencia=repository.exemplar(id,false);
        // Mesmo contrato de H7/H8: livro -> exemplar. O vínculo com o livro é imutável.
        repository.livro(referencia.livroId(),true);
        return repository.exemplar(id,true);
    }
    private String tombo(String valor,long excetoId) {
        String tombo=valor.trim();
        if (tombo.isEmpty() || tombo.length()>50) throw new IllegalArgumentException("Código de tombo inválido");
        if (repository.tomboOcupado(tombo,excetoId)) throw new ConflitoException("Já existe exemplar com este código de tombo");
        return tombo;
    }
    private void positivo(long id) { if (id<=0) throw new IllegalArgumentException("ID deve ser positivo"); }
    private void paginacao(int pagina,int tamanho) {
        if (pagina<0 || tamanho<1 || tamanho>100) throw new IllegalArgumentException("Paginação inválida");
    }
}

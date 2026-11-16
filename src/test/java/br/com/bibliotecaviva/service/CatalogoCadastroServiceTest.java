package br.com.bibliotecaviva.service;

import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import br.com.bibliotecaviva.dto.*;
import br.com.bibliotecaviva.exception.*;
import br.com.bibliotecaviva.repository.CatalogoCadastroRepository;
import br.com.bibliotecaviva.repository.CatalogoCadastroRepository.Tipo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogoCadastroServiceTest {
    @Mock CatalogoCadastroRepository repo;
    @InjectMocks CatalogoCadastroService service;
    LivroResponse livro(boolean ativo) { return new LivroResponse(1,"Obra","1234567890123",2000,null,ativo,List.of(),List.of()); }
    LivroRequest pedido(String isbn,List<Long> autores) { return new LivroRequest(" Obra ",isbn,2000,null,true,autores,List.of(1L)); }
    ExemplarResponse exemplar(String status) { return new ExemplarResponse(2,1,"T1","bom",status,null); }
    void travas(String status) {
        when(repo.exemplar(2,false)).thenReturn(exemplar(status));
        when(repo.livro(1,true)).thenReturn(livro(true));
        when(repo.exemplar(2,true)).thenReturn(exemplar(status));
    }
    @Test void normalizaIsbnEVinculaVariosAutores() {
        var r=pedido("123-456-7890-123",List.of(2L,1L));
        when(repo.criarLivro(r,"1234567890123")).thenReturn(1L);
        when(repo.livro(1,false)).thenReturn(livro(true));
        service.criarLivro(r);
        verify(repo).conferirReferencias(Tipo.AUTOR,List.of(2L,1L));
        verify(repo).substituirVinculos(1,Tipo.AUTOR,List.of(2L,1L));
    }
    @Test void permiteIsbn10ComX() {
        var r=pedido("012345678x",List.of());
        when(repo.criarLivro(r,"012345678X")).thenReturn(1L);
        service.criarLivro(r);
        verify(repo).criarLivro(r,"012345678X");
    }
    @Test void recusaFormatoIsbnInvalidoAntesDeGravar() {
        assertThrows(IllegalArgumentException.class,()->service.criarLivro(pedido("ABCDEFGHIJKLM",List.of())));
        verifyNoInteractions(repo);
    }
    @Test void recusaIsbnDuplicado() {
        when(repo.isbnOcupado("1234567890123",0)).thenReturn(true);
        assertThrows(ConflitoException.class,()->service.criarLivro(pedido("1234567890123",List.of())));
        verify(repo,never()).criarLivro(any(),anyString());
    }
    @Test void recusaVinculosRepetidos() {
        assertThrows(IllegalArgumentException.class,()->service.criarLivro(pedido("1234567890123",List.of(1L,1L))));
        verify(repo,never()).criarLivro(any(),anyString());
    }
    @Test void recusaVinculoInexistenteAntesDeGravar() {
        doThrow(new RecursoNaoEncontradoException("Autor ausente")).when(repo).conferirReferencias(Tipo.AUTOR,List.of(999L));
        assertThrows(RecursoNaoEncontradoException.class,()->service.criarLivro(pedido("1234567890123",List.of(999L))));
        verify(repo,never()).criarLivro(any(),anyString());
    }
    @Test void editaLivroMantendoId() {
        var r=pedido("1234567890123",List.of(2L));
        when(repo.livro(1,true)).thenReturn(livro(true));
        service.atualizarLivro(1,r);
        verify(repo).isbnOcupado("1234567890123",1);
        verify(repo).atualizarLivro(1,r,"1234567890123");
        verify(repo).substituirVinculos(1,Tipo.AUTOR,List.of(2L));
    }
    @Test void cadastrosOpcionaisTemDefaults() {
        var r=new LivroRequest("Livro","1234567890123",null,null,null,null,null);
        assertTrue(r.ativo()); assertEquals(List.of(),r.autorIds()); assertEquals(List.of(),r.categoriaIds());
    }
    @Test void categoriaRespeitaLimiteDoSchema() {
        assertThrows(IllegalArgumentException.class,()->service.criarReferencia(Tipo.CATEGORIA,new NomeCadastroRequest("x".repeat(81))));
        verifyNoInteractions(repo);
    }
    @Test void nomeReferenciaSemEspacosExternos() {
        service.criarReferencia(Tipo.AUTOR,new NomeCadastroRequest("  Clarice  "));
        verify(repo).criarReferencia(Tipo.AUTOR,"Clarice");
    }
    @Test void recusaPaginacaoInvalida() {
        assertThrows(IllegalArgumentException.class,()->service.listarLivros(-1,20));
        assertThrows(IllegalArgumentException.class,()->service.listarExemplares(null,0,101));
        assertThrows(IllegalArgumentException.class,()->service.listarReferencias(Tipo.AUTOR,0,0));
        verifyNoInteractions(repo);
    }
    @Test void criaExemplarComTomboNormalizado() {
        when(repo.livro(1,true)).thenReturn(livro(true));
        var r=new ExemplarCreateRequest(1L," T1 ","bom");
        service.criarExemplar(r);
        verify(repo).criarExemplar(r,"T1");
    }
    @Test void livroInativoNaoRecebeExemplar() {
        when(repo.livro(1,true)).thenReturn(livro(false));
        assertThrows(ConflitoException.class,()->service.criarExemplar(new ExemplarCreateRequest(1L,"T1","bom")));
        verify(repo,never()).criarExemplar(any(),anyString());
    }
    @Test void tomboDuplicadoNaoCriaExemplar() {
        when(repo.livro(1,true)).thenReturn(livro(true)); when(repo.tomboOcupado("T1",0)).thenReturn(true);
        assertThrows(ConflitoException.class,()->service.criarExemplar(new ExemplarCreateRequest(1L,"T1","bom")));
    }
    @Test void editaExemplarSemMudarStatusNemVinculo() {
        travas("emprestado"); var r=new ExemplarUpdateRequest("T2","regular");
        service.atualizarExemplar(2,r);
        InOrder ordem=inOrder(repo); ordem.verify(repo).exemplar(2,false); ordem.verify(repo).livro(1,true); ordem.verify(repo).exemplar(2,true);
        verify(repo).atualizarExemplar(2,r,"T2"); verify(repo,never()).situacao(anyLong(),anyBoolean());
    }
    @Test void recusaInativarEmprestado() {
        travas("emprestado"); assertThrows(ConflitoException.class,()->service.situacaoExemplar(2,false));
        verify(repo,never()).situacao(anyLong(),anyBoolean());
    }
    @Test void recusaInativarReservado() {
        travas("reservado"); assertThrows(ConflitoException.class,()->service.situacaoExemplar(2,false));
    }
    @Test void recusaStatusInconsistenteComMovimentacaoAtiva() {
        travas("disponivel"); when(repo.movimentacaoAtiva(2)).thenReturn(true);
        assertThrows(ConflitoException.class,()->service.situacaoExemplar(2,false));
    }
    @Test void inativaDisponivelSemMovimentacao() {
        travas("disponivel"); service.situacaoExemplar(2,false); verify(repo).situacao(2,false);
    }
    @Test void reativaSomenteSeLivroAtivo() {
        travas("inativo"); when(repo.livro(1,false)).thenReturn(livro(false));
        assertThrows(ConflitoException.class,()->service.situacaoExemplar(2,true));
    }
    @Test void reativaExemplarLiberado() {
        travas("inativo"); when(repo.livro(1,false)).thenReturn(livro(true));
        service.situacaoExemplar(2,true); verify(repo).situacao(2,true);
    }
    @Test void idsInvalidosNaoConsultamBanco() {
        assertThrows(IllegalArgumentException.class,()->service.buscarLivro(0));
        assertThrows(IllegalArgumentException.class,()->service.buscarExemplar(-1));
        verifyNoInteractions(repo);
    }
}

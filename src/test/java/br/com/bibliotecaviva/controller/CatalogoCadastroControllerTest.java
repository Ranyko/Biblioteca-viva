package br.com.bibliotecaviva.controller;

import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import br.com.bibliotecaviva.dto.*;
import br.com.bibliotecaviva.exception.GlobalExceptionHandler;
import br.com.bibliotecaviva.service.CatalogoCadastroService;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** MVC isolado: permissões reais são verificadas em CatalogoPostgresTest. */
class CatalogoCadastroControllerTest {
    CatalogoCadastroService service;
    MockMvc mvc;
    @BeforeEach void preparar() {
        service=mock(CatalogoCadastroService.class);
        mvc=MockMvcBuilders.standaloneSetup(new CatalogoCadastroController(service))
            .setControllerAdvice(new GlobalExceptionHandler()).build();
    }
    @Test void livroSemIsbnRetorna400CT04() throws Exception {
        mvc.perform(post("/admin/livros").contentType(MediaType.APPLICATION_JSON).content("{\"titulo\":\"Livro\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.isbn").exists());
        verifyNoInteractions(service);
    }
    @Test void livroCriadoTemLocationEAutores() throws Exception {
        when(service.criarLivro(any())).thenReturn(new LivroResponse(7,"Livro","1234567890123",null,null,true,
            List.of(new ReferenciaResponse(1,"Autor")),List.of()));
        mvc.perform(post("/admin/livros").contentType(MediaType.APPLICATION_JSON)
            .content("{\"titulo\":\"Livro\",\"isbn\":\"1234567890123\"}"))
            .andExpect(status().isCreated()).andExpect(header().string("Location","/admin/livros/7"))
            .andExpect(jsonPath("$.autores[0].id").value(1));
    }
    @Test void conservacaoInvalidaNaoChamaServico() throws Exception {
        mvc.perform(post("/admin/exemplares").contentType(MediaType.APPLICATION_JSON)
            .content("{\"livroId\":1,\"codigoTombo\":\"T1\",\"estadoConservacao\":\"excelente\"}"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
    @Test void livroComVinculoNaoPositivoRetorna400() throws Exception {
        mvc.perform(post("/admin/livros").contentType(MediaType.APPLICATION_JSON)
            .content("{\"titulo\":\"Livro\",\"isbn\":\"1234567890123\",\"autorIds\":[0]}"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
    @Test void situacaoSemAtivoRetorna400() throws Exception {
        mvc.perform(patch("/admin/exemplares/1/situacao").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}

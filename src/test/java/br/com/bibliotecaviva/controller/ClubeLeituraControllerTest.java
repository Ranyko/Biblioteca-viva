package br.com.bibliotecaviva.controller;

import java.time.OffsetDateTime;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import br.com.bibliotecaviva.dto.ClubeLeituraRequest;
import br.com.bibliotecaviva.dto.ClubeLeituraResponse;
import br.com.bibliotecaviva.dto.EncontroClubeResponse;
import br.com.bibliotecaviva.exception.GlobalExceptionHandler;
import br.com.bibliotecaviva.model.Usuario;
import br.com.bibliotecaviva.service.ClubeLeituraService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClubeLeituraControllerTest {
    private ClubeLeituraService service;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        service = mock(ClubeLeituraService.class);
        mvc = MockMvcBuilders.standaloneSetup(new AdminClubeLeituraController(service),
            new EncontroClubeController(service), new LeitorClubeLeituraController(service))
            .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void clubeCriadoRetornaLocation() throws Exception {
        var criado = new ClubeLeituraResponse(3, "Clube", null, "ativo", 12, null, 1,
            OffsetDateTime.parse("2026-10-01T10:00:00-03:00"), List.of());
        when(service.criarClube(any(), eq(1L))).thenReturn(criado);
        Usuario usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(1L);

        var response = new AdminClubeLeituraController(service).criar(
            new ClubeLeituraRequest("Clube", null, 12, null, List.of()), usuario);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(URI.create("/admin/clubes-leitura/3"), response.getHeaders().getLocation());
        assertEquals(criado, response.getBody());
    }

    @Test
    void capacidadeAusenteOuNaoPositivaRetorna400() throws Exception {
        for (String json : List.of("{\"nome\":\"Clube\"}",
                "{\"nome\":\"Clube\",\"capacidadeMaxima\":0}")) {
            mvc.perform(post("/admin/clubes-leitura").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }

    @Test
    void statusDeClubeAceitaSomenteValoresConhecidos() throws Exception {
        mvc.perform(patch("/admin/clubes-leitura/3/status").contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"removido\"}")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void dataOuLivroAusentesImpedemCriacaoDeEncontro() throws Exception {
        mvc.perform(post("/atendente/clubes-leitura/3/encontros").contentType(MediaType.APPLICATION_JSON)
            .content("{\"livroId\":7,\"localOuAcesso\":\"Sala\"}"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void encontroCriadoRetornaLocation() throws Exception {
        when(service.criarEncontro(eq(3L), any())).thenReturn(
            new EncontroClubeResponse(5, 3, "Clube", 7, "Livro",
                OffsetDateTime.parse("2026-10-02T10:00:00-03:00"), "Sala", "aberto",
                12, 0, 12, OffsetDateTime.parse("2026-10-01T10:00:00-03:00")));

        mvc.perform(post("/atendente/clubes-leitura/3/encontros").contentType(MediaType.APPLICATION_JSON)
                .content("{\"livroId\":7,\"dataHora\":\"2026-10-02T10:00:00-03:00\",\"localOuAcesso\":\"Sala\"}"))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/atendente/clubes-leitura/3/encontros/5"))
            .andExpect(jsonPath("$.vagasDisponiveis").value(12));
    }
}

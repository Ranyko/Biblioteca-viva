package br.com.bibliotecaviva.controller;

import java.time.*;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import br.com.bibliotecaviva.dto.*;
import br.com.bibliotecaviva.exception.*;
import br.com.bibliotecaviva.model.Usuario;
import br.com.bibliotecaviva.service.CirculacaoService;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Contrato MVC isolado; RBAC/JWT e SQL são cobertos pela suíte PostgreSQL, não por estes mocks. */
class CirculacaoControllerTest {
    CirculacaoService service;
    MockMvc mvc;
    @BeforeEach void preparar() {
        service = mock(CirculacaoService.class);
        Usuario usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", 4L);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
        mvc = MockMvcBuilders.standaloneSetup(new CirculacaoController(service))
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .setControllerAdvice(new GlobalExceptionHandler()).build();
    }
    @AfterEach void limpar() { SecurityContextHolder.clearContext(); }
    @Test void retornoCreatedTemLocationEDatasSerializadas() throws Exception {
        var em = new EmprestimoResponse(9L, 1L, 2L, 4L, null, OffsetDateTime.parse("2026-10-01T10:00:00-03:00"),
            LocalDate.parse("2026-10-15"), null);
        when(service.emprestar(any(), eq(4L))).thenReturn(em);
        mvc.perform(post("/atendente/emprestimos").contentType(MediaType.APPLICATION_JSON).content("{\"leitorId\":1,\"exemplarId\":2}"))
            .andExpect(status().isCreated()).andExpect(header().string("Location", "/atendente/emprestimos/9"))
            .andExpect(jsonPath("$.id").value(9)).andExpect(jsonPath("$.dataPrevistaDevolucao").value("2026-10-15"));
    }
    @Test void validacaoBeanRecusaIdsInvalidos() throws Exception {
        mvc.perform(post("/atendente/emprestimos").contentType(MediaType.APPLICATION_JSON).content("{\"leitorId\":0}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.leitorId").exists())
            .andExpect(jsonPath("$.campos.exemplarId").exists());
        verifyNoInteractions(service);
    }
    @Test void jsonMalformadoRetorna400() throws Exception {
        mvc.perform(post("/atendente/emprestimos").contentType(MediaType.APPLICATION_JSON).content("{errado"))
            .andExpect(status().isBadRequest());
    }
    @Test void conflitoDeRegraRetorna409() throws Exception {
        when(service.devolver(1, 4)).thenThrow(new ConflitoException("Empréstimo já devolvido"));
        mvc.perform(post("/atendente/emprestimos/1/devolucao")).andExpect(status().isConflict())
            .andExpect(jsonPath("$.mensagem").value("Empréstimo já devolvido"));
    }
}

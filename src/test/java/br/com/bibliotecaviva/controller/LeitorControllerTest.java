package br.com.bibliotecaviva.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import br.com.bibliotecaviva.model.Leitor;
import br.com.bibliotecaviva.model.PerfilUsuario;
import br.com.bibliotecaviva.model.Usuario;
import br.com.bibliotecaviva.repository.LeitorRepository;
import br.com.bibliotecaviva.repository.UsuarioRepository;
import br.com.bibliotecaviva.service.SugestaoLivroService;
import br.com.bibliotecaviva.service.TokenService;
import br.com.bibliotecaviva.dto.PainelLeitorResponse;
import br.com.bibliotecaviva.service.PainelLeitorService;

@SpringBootTest
@AutoConfigureMockMvc
class LeitorControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private LeitorRepository leitorRepository;

        @MockitoBean
        private SugestaoLivroService sugestaoLivroService;

        @MockitoBean
        private TokenService tokenService;

        @MockitoBean
        private UsuarioRepository usuarioRepository;

        @MockitoBean
        private PainelLeitorService painelLeitorService;

        private Usuario leitorUsuario;
        private Leitor leitor;
        private Usuario atendenteUsuario;

        @BeforeEach
        void setUp() {
                leitorUsuario = mock(Usuario.class);
                when(leitorUsuario.getId()).thenReturn(10L);
                when(leitorUsuario.getPerfil()).thenReturn(PerfilUsuario.LEITOR);
                when(leitorUsuario.isAtivo()).thenReturn(true);

                leitor = mock(Leitor.class);
                when(leitor.getId()).thenReturn(20L);
        }

        @Test
        void deveRetornar401QuandoNaoAutenticado() throws Exception {
                mockMvc.perform(get("/leitor/sugestoes"))
                                .andExpect(status().isUnauthorized());

                verifyNoInteractions(leitorRepository, sugestaoLivroService);
        }

        @Test
        void deveRetornar200EConsultarSugestoesDoLeitorAutenticado()
                        throws Exception {
                when(tokenService.validarEObterId("token-leitor"))
                                .thenReturn(Optional.of(10L));
                when(usuarioRepository.findById(10L))
                                .thenReturn(Optional.of(leitorUsuario));
                when(leitorRepository.findByUsuarioId(10L))
                                .thenReturn(Optional.of(leitor));
                when(sugestaoLivroService.buscarSugestoes(20L))
                                .thenReturn(List.of());

                mockMvc.perform(get("/leitor/sugestoes")
                                .header("Authorization", "Bearer token-leitor"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());

                verify(leitorRepository).findByUsuarioId(10L);
                verify(sugestaoLivroService).buscarSugestoes(20L);
        }

        @Test
        void naoDevePermitirAcessoDeAtendente() throws Exception {
                atendenteUsuario = mock(Usuario.class);

                when(atendenteUsuario.getId()).thenReturn(30L);
                when(atendenteUsuario.getPerfil()).thenReturn(PerfilUsuario.ATENDENTE);
                when(atendenteUsuario.isAtivo()).thenReturn(true);

                when(tokenService.validarEObterId("token-atendente"))
                                .thenReturn(Optional.of(30L));
                when(usuarioRepository.findById(30L))
                                .thenReturn(Optional.of(atendenteUsuario));

                mockMvc.perform(get("/leitor/sugestoes")
                                .header("Authorization", "Bearer token-atendente"))
                                .andExpect(status().isForbidden());

                verifyNoInteractions(leitorRepository, sugestaoLivroService);
        }

        @Test
        void deveRetornar401QuandoNaoAutenticadoAoConsultarPainel() throws Exception {
                mockMvc.perform(get("/leitor/meu-painel"))
                                .andExpect(status().isUnauthorized());

                verifyNoInteractions(leitorRepository, painelLeitorService);
        }

        @Test
        void deveRetornar200EConsultarPainelDoLeitorAutenticado()
                        throws Exception {
                when(tokenService.validarEObterId("token-leitor"))
                                .thenReturn(Optional.of(10L));
                when(usuarioRepository.findById(10L))
                                .thenReturn(Optional.of(leitorUsuario));
                when(leitorRepository.findByUsuarioId(10L))
                                .thenReturn(Optional.of(leitor));

                when(painelLeitorService.consultar(20L))
                                .thenReturn(new PainelLeitorResponse(
                                                List.of(), List.of(), List.of()));

                mockMvc.perform(get("/leitor/meu-painel")
                                .header("Authorization", "Bearer token-leitor"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.emprestimos").isArray())
                                .andExpect(jsonPath("$.reservas").isArray())
                                .andExpect(jsonPath("$.multas").isArray());

                verify(leitorRepository).findByUsuarioId(10L);
                verify(painelLeitorService).consultar(20L);
        }

        @Test
        void deveRetornar404QuandoLeitorNaoPossuiCadastro()
                        throws Exception {
                when(tokenService.validarEObterId("token-leitor"))
                                .thenReturn(Optional.of(10L));
                when(usuarioRepository.findById(10L))
                                .thenReturn(Optional.of(leitorUsuario));
                when(leitorRepository.findByUsuarioId(10L))
                                .thenReturn(Optional.empty());

                mockMvc.perform(get("/leitor/meu-painel")
                                .header("Authorization", "Bearer token-leitor"))
                                .andExpect(status().isNotFound());

                verify(leitorRepository).findByUsuarioId(10L);
                verifyNoInteractions(painelLeitorService);
        }

        @Test
        void naoDevePermitirAcessoDeAtendenteAoPainel() throws Exception {
                atendenteUsuario = mock(Usuario.class);

                when(atendenteUsuario.getId()).thenReturn(30L);
                when(atendenteUsuario.getPerfil()).thenReturn(PerfilUsuario.ATENDENTE);
                when(atendenteUsuario.isAtivo()).thenReturn(true);

                when(tokenService.validarEObterId("token-atendente"))
                                .thenReturn(Optional.of(30L));
                when(usuarioRepository.findById(30L))
                                .thenReturn(Optional.of(atendenteUsuario));

                mockMvc.perform(get("/leitor/meu-painel")
                                .header("Authorization", "Bearer token-atendente"))
                                .andExpect(status().isForbidden());

                verifyNoInteractions(leitorRepository, painelLeitorService);
        }
}

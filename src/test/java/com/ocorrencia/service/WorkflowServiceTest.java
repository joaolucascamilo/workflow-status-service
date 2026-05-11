package com.ocorrencia.service;

import com.ocorrencia.client.OcorrenciasClient;
import com.ocorrencia.client.UsuariosClient;
import com.ocorrencia.domain.StatusOcorrencia;
import com.ocorrencia.dto.OcorrenciaDTO;
import com.ocorrencia.entity.HistoricoWorkflow;
import com.ocorrencia.repository.HistoricoWorkflowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock
    private HistoricoWorkflowRepository repository;

    @Mock
    private UsuariosClient usuariosClient;

    @Mock
    private OcorrenciasClient ocorrenciasClient;

    @InjectMocks
    private WorkflowService workflowService;

    private final Long ocorrenciaIdPadrao = 1L;
    private final String tokenPadrao = "Bearer token-mock";

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Deve permitir registrar uma nova ocorrência (status inicial)")
    void deveRegistrarNovaOcorrencia() {
        // Arrange
        // Mock necessário para passar pela validação inicial (try/catch novo)
        when(ocorrenciasClient.buscarPorId(ocorrenciaIdPadrao)).thenReturn(new OcorrenciaDTO(1L,1L));

        when(repository.findTopByOcorrenciaIdOrderByDataAtualizacaoDesc(ocorrenciaIdPadrao))
                .thenReturn(Optional.empty());

        HistoricoWorkflow historicoSalvo = new HistoricoWorkflow();
        historicoSalvo.setStatus(StatusOcorrencia.REGISTRADO);
        when(repository.save(any(HistoricoWorkflow.class))).thenReturn(historicoSalvo);

        // Act - Passando o parâmetro 'tokenPadrao'
        HistoricoWorkflow resultado = workflowService.atualizarStatus(
                ocorrenciaIdPadrao, StatusOcorrencia.REGISTRADO.getCodigo(), "Primeiro registro", tokenPadrao
        );

        // Assert
        assertNotNull(resultado);
        assertEquals(StatusOcorrencia.REGISTRADO, resultado.getStatus());
        verify(repository, times(1)).save(any(HistoricoWorkflow.class));

        // Verifica se a atualização de status no microsserviço ocorreu
        verify(ocorrenciasClient, times(1))
                .atualizarStatusOcorrencia(ocorrenciaIdPadrao, StatusOcorrencia.REGISTRADO.getCodigo(), tokenPadrao);

        // Verifica que NADA foi feito com gamificação
        verifyNoInteractions(usuariosClient);
    }

    @Test
    @DisplayName("Deve chamar os clientes de gamificação ao resolver a ocorrência")
    void deveDispararGamificacaoAoResolver() {
        // Arrange
        Long usuarioId = 10L;

        OcorrenciaDTO ocorrenciaMock = mock(OcorrenciaDTO.class);
        when(ocorrenciaMock.getId()).thenReturn(ocorrenciaIdPadrao);
        when(ocorrenciaMock.getUsuarioId()).thenReturn(usuarioId);

        // Mock para a busca inicial E para o método 'dispararEventoResolvido'
        when(ocorrenciasClient.buscarPorId(ocorrenciaIdPadrao)).thenReturn(ocorrenciaMock);

        HistoricoWorkflow historicoAtual = new HistoricoWorkflow();
        historicoAtual.setStatus(StatusOcorrencia.EM_PROCEDIMENTO);
        when(repository.findTopByOcorrenciaIdOrderByDataAtualizacaoDesc(ocorrenciaIdPadrao))
                .thenReturn(Optional.of(historicoAtual));

        HistoricoWorkflow historicoResolvido = new HistoricoWorkflow();
        historicoResolvido.setStatus(StatusOcorrencia.RESOLVIDO);
        when(repository.save(any(HistoricoWorkflow.class))).thenReturn(historicoResolvido);

        // Act
        workflowService.atualizarStatus(ocorrenciaIdPadrao, StatusOcorrencia.RESOLVIDO.getCodigo(), "Buraco tapado!", tokenPadrao);

        // Assert
        // Verifica se buscarPorId foi chamado duas vezes (na validação inicial e no disparo do evento)
        verify(ocorrenciasClient, times(2)).buscarPorId(ocorrenciaIdPadrao);

        // Verifica a chamada de sincronização no MS de ocorrências
        verify(ocorrenciasClient, times(1))
                .atualizarStatusOcorrencia(ocorrenciaIdPadrao, StatusOcorrencia.RESOLVIDO.getCodigo(), tokenPadrao);

        String mensagemEsperada = "Ocorrência validada: id" + ocorrenciaIdPadrao;

        // Verifica o payload de gamificação
        verify(usuariosClient, times(1)).adicionarPontos(argThat(payload ->
                payload.getUsuarioId().equals(usuarioId) &&
                        payload.getPontos().equals(50) &&
                        payload.getDescricao().equals(mensagemEsperada)
        ));
    }
}
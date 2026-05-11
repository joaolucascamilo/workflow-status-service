package com.ocorrencia.service;

import com.ocorrencia.client.OcorrenciasClient;
import com.ocorrencia.client.UsuariosClient;
import com.ocorrencia.domain.StatusOcorrencia;
import com.ocorrencia.dto.OcorrenciaDTO;
import com.ocorrencia.dto.PontuacaoRequestDTO;
import com.ocorrencia.entity.HistoricoWorkflow;
import com.ocorrencia.repository.HistoricoWorkflowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkflowService {

    private final HistoricoWorkflowRepository repository;
    private final UsuariosClient usuariosClient;
    private final OcorrenciasClient ocorrenciasClient;

    public WorkflowService(HistoricoWorkflowRepository repository, UsuariosClient usuariosClient, OcorrenciasClient ocorrenciasClient) {
        this.repository = repository;
        this.usuariosClient = usuariosClient;
        this.ocorrenciasClient = ocorrenciasClient;
    }

    @Transactional
    public HistoricoWorkflow atualizarStatus(Long ocorrenciaId, Integer novoStatus, String observacao, String token) {

        try {
            OcorrenciaDTO ocorrencia = ocorrenciasClient.buscarPorId(ocorrenciaId);
            if (ocorrencia == null){
                throw new RuntimeException("Ocorrência de ID " + ocorrenciaId + " não encontrada no sistema principal.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha ao consultar ocorrência no serviço de ocorrências");
        }



        //Busca o status atual (o último registro dessa ocorrência)
        StatusOcorrencia statusAtual = buscarStatusAtual(ocorrenciaId);

        if (novoStatus.equals(statusAtual)) {
            throw new RuntimeException("A ocorrência já se encontra no status: " + novoStatus);
        }

        // Valida se a transição é permitida
        validarTransicao(statusAtual);

        HistoricoWorkflow novoHistorico = new HistoricoWorkflow();
        novoHistorico.setOcorrenciaId(ocorrenciaId);
        novoHistorico.setStatus(StatusOcorrencia.valueOf(novoStatus));
        novoHistorico.setDataAtualizacao(LocalDateTime.now());
        novoHistorico.setObservacao(observacao);

        HistoricoWorkflow salvo = repository.save(novoHistorico);

        try {
            ocorrenciasClient.atualizarStatusOcorrencia(ocorrenciaId, novoStatus, token);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao sincronizar o novo status com o microsserviço de ocorrências: " + e.getMessage());
        }

        //Se o novo status for RESOLVIDO, dispara a notificação para gamificação
        if (novoStatus.equals(StatusOcorrencia.RESOLVIDO.getCodigo())) {
            dispararEventoResolvido(ocorrenciaId);
        }

        return salvo;
    }

    public StatusOcorrencia buscarStatusAtual(Long ocorrenciaId) {
        return repository.findTopByOcorrenciaIdOrderByDataAtualizacaoDesc(ocorrenciaId)
                .map(HistoricoWorkflow::getStatus)
                .orElse(null);
    }

    private void validarTransicao(StatusOcorrencia atual) {
        if (atual == StatusOcorrencia.RESOLVIDO) {
            throw new RuntimeException("Uma ocorrência resolvida não pode mudar de status.");
        }
    }

    private void dispararEventoResolvido(Long ocorrenciaId) {
        try {
            OcorrenciaDTO ocorrencia = ocorrenciasClient.buscarPorId(ocorrenciaId);

            Long idDoCidadao = ocorrencia.getUsuarioId();

            if (idDoCidadao != null) {
                String mensagemGamificacao = "Ocorrência validada: id" + ocorrencia.getId();
                Integer pontosPadrao = 50;

                PontuacaoRequestDTO payload = new PontuacaoRequestDTO(
                        idDoCidadao,
                        pontosPadrao,
                        mensagemGamificacao
                );

                usuariosClient.adicionarPontos(payload);
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha ao processar a gamificação para a ocorrência " + ocorrenciaId + ": " + e.getMessage());
        }
    }
}

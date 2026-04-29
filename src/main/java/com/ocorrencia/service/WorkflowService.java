package com.ocorrencia.service;

import com.ocorrencia.client.UsuariosClient;
import com.ocorrencia.domain.StatusOcorrencia;
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

    public WorkflowService(HistoricoWorkflowRepository repository, UsuariosClient usuariosClient) {
        this.repository = repository;
        this.usuariosClient = usuariosClient;
    }

    @Transactional
    public HistoricoWorkflow atualizarStatus(Long ocorrenciaId, StatusOcorrencia novoStatus, String observacao) {
        // 1. Busca o status atual (o último registro dessa ocorrência)
        StatusOcorrencia statusAtual = buscarStatusAtual(ocorrenciaId);

        // 2. Valida se a transição é permitida
        validarTransicao(statusAtual, novoStatus);

        // 3. Salva o novo histórico
        HistoricoWorkflow novoHistorico = new HistoricoWorkflow();
        novoHistorico.setOcorrenciaId(ocorrenciaId);
        novoHistorico.setStatus(novoStatus);
        novoHistorico.setDataAtualizacao(LocalDateTime.now());
        novoHistorico.setObservacao(observacao);

        HistoricoWorkflow salvo = repository.save(novoHistorico);

        // 4. Se o novo status for RESOLVIDO, dispara a notificação para gamificação
        if (novoStatus == StatusOcorrencia.RESOLVIDO) {
            dispararEventoResolvido(ocorrenciaId);
        }

        return salvo;
    }

    public StatusOcorrencia buscarStatusAtual(Long ocorrenciaId) {
        return repository.findTopByOcorrenciaIdOrderByDataAtualizacaoDesc(ocorrenciaId)
                .map(HistoricoWorkflow::getStatus)
                .orElse(null);
    }

    private void validarTransicao(StatusOcorrencia atual, StatusOcorrencia novo) {
        if (atual == null && novo != StatusOcorrencia.REGISTRADO) {
            throw new RuntimeException("O primeiro status deve ser REGISTRADO.");
        }
        if (atual == StatusOcorrencia.RESOLVIDO) {
            throw new RuntimeException("Uma ocorrência resolvida não pode mudar de status.");
        }
        // Aqui você pode adicionar mais regras específicas do "mini Jira"
    }

    private void dispararEventoResolvido(Long ocorrenciaId) {
        // TODO: Chamar o MS 5 (Usuários) para adicionar pontos de reputação
        System.out.println("Disparando evento para o MS 5: Ocorrência " + ocorrenciaId + " foi resolvida!");
    }
}

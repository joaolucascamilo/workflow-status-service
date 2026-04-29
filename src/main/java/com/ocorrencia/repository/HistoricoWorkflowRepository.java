package com.ocorrencia.repository;

import com.ocorrencia.entity.HistoricoWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HistoricoWorkflowRepository extends JpaRepository<HistoricoWorkflow, Long> {
    /*
        O Spring lê o nome do método usando suas palavras-chave
        (findTopBy + OcorrenciaId + OrderBy + DataAtualizacao + Desc) e traduz isso automaticamente para uma query SQL
        por trás dos panos na hora que a aplicação sobe
    */
    Optional<HistoricoWorkflow> findTopByOcorrenciaIdOrderByDataAtualizacaoDesc(Long ocorrenciaId);
}

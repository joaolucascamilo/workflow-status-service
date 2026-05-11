package com.ocorrencia.controller;

import com.ocorrencia.domain.StatusOcorrencia;
import com.ocorrencia.entity.HistoricoWorkflow;
import com.ocorrencia.service.WorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

    private final WorkflowService service;

    public WorkflowController(WorkflowService service) {
        this.service = service;
    }

    @PostMapping("/{ocorrenciaId}/status")
    public ResponseEntity<HistoricoWorkflow> atualizarStatus(
            @PathVariable Long ocorrenciaId,
            @RequestParam Integer status,
            @RequestParam(required = false) String observacao,
            @RequestHeader("Authorization") String token) {

        HistoricoWorkflow historico = service.atualizarStatus(ocorrenciaId, status, observacao, token);
        return ResponseEntity.ok(historico);
    }

    @GetMapping("/{ocorrenciaId}/atual")
    public ResponseEntity<StatusOcorrencia> getStatusAtual(@PathVariable Long ocorrenciaId) {
        // Implementar no service a busca do status atual para exibir no app
        return ResponseEntity.ok(service.buscarStatusAtual(ocorrenciaId));
    }
}

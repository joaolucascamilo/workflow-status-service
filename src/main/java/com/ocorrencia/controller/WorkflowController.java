package com.ocorrencia.controller;

import com.ocorrencia.domain.StatusOcorrencia;
import com.ocorrencia.dto.ErroResponse;
import com.ocorrencia.entity.HistoricoWorkflow;
import com.ocorrencia.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflow")
@Tag(name = "Workflow", description = "Gerenciamento do ciclo de vida de status das ocorrências")
public class WorkflowController {

    private final WorkflowService service;

    public WorkflowController(WorkflowService service) {
        this.service = service;
    }

    @Operation(
            summary = "Atualizar status de uma ocorrência",
            description = """
                    Registra uma nova transição de status para a ocorrência informada e sincroniza a mudança
                    com o serviço de ocorrências. Caso o novo status seja **RESOLVIDO** (3), concede automaticamente
                    50 pontos ao usuário associado via serviço de gamificação.

                    **Códigos de status aceitos:**
                    | Código | Status |
                    |--------|--------|
                    | 1 | REGISTRADO |
                    | 2 | CANCELADO |
                    | 3 | RESOLVIDO |
                    | 4 | EM_PROCEDIMENTO |
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Status atualizado com sucesso. Retorna o registro de histórico criado.",
                    content = @Content(schema = @Schema(implementation = HistoricoWorkflow.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Regra de negócio violada (status inválido, ocorrência já resolvida, status repetido, etc.)",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))
            )
    })
    @PostMapping("/{ocorrenciaId}/status")
    public ResponseEntity<HistoricoWorkflow> atualizarStatus(
            @Parameter(description = "ID da ocorrência a ter o status atualizado", example = "7", required = true)
            @PathVariable Long ocorrenciaId,

            @Parameter(description = "Código numérico do novo status (1=REGISTRADO, 2=CANCELADO, 3=RESOLVIDO, 4=EM_PROCEDIMENTO)", example = "4", required = true)
            @RequestParam Integer status,

            @Parameter(description = "Observação opcional sobre a transição", example = "Equipe enviada ao local")
            @RequestParam(required = false) String observacao,

            @Parameter(description = "Token JWT de autenticação no formato 'Bearer {token}'", required = true, example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader("Authorization") String token) {

        HistoricoWorkflow historico = service.atualizarStatus(ocorrenciaId, status, observacao, token);
        return ResponseEntity.ok(historico);
    }

    @Operation(
            summary = "Consultar status atual de uma ocorrência",
            description = "Retorna o status mais recente registrado no histórico da ocorrência informada. " +
                    "Retorna `null` se a ocorrência ainda não possui nenhum registro de status neste serviço."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Status atual da ocorrência (pode ser null se não houver histórico)",
                    content = @Content(schema = @Schema(implementation = StatusOcorrencia.class))
            )
    })
    @GetMapping("/{ocorrenciaId}/atual")
    public ResponseEntity<StatusOcorrencia> getStatusAtual(
            @Parameter(description = "ID da ocorrência", example = "7", required = true)
            @PathVariable Long ocorrenciaId) {
        return ResponseEntity.ok(service.buscarStatusAtual(ocorrenciaId));
    }
}

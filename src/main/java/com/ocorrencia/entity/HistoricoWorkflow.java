package com.ocorrencia.entity;

import com.ocorrencia.domain.StatusOcorrencia;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "historico_workflow")
@Schema(description = "Registro de uma transição de status no ciclo de vida de uma ocorrência")
public class HistoricoWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do registro de histórico", example = "42")
    private Long id;

    @Column(name = "ocorrencia_id", nullable = false)
    @Schema(description = "ID da ocorrência no serviço de ocorrências (ms-ocorrencias)", example = "7")
    private Long ocorrenciaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Status atribuído à ocorrência nesta transição", example = "EM_PROCEDIMENTO")
    private StatusOcorrencia status;

    @Column(name = "data_atualizacao", nullable = false)
    @Schema(description = "Data e hora em que o status foi registrado", example = "2026-06-28T14:30:00")
    private LocalDateTime dataAtualizacao;

    @Column(name = "observacao")
    @Schema(description = "Observação opcional sobre a transição de status", example = "Equipe enviada ao local")
    private String observacao;
}

package com.ocorrencia.entity;

import com.ocorrencia.domain.StatusOcorrencia;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "historico_workflow")
public class HistoricoWorkflow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID da ocorrência gerado pelo Microserviço 1
    @Column(name = "ocorrencia_id", nullable = false)
    private Long ocorrenciaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOcorrencia status;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @Column(name = "observacao")
    private String observacao; // Ex: "Equipe enviada ao local"
}

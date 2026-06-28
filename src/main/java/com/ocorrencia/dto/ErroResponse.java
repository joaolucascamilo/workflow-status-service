package com.ocorrencia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de erro retornada quando uma regra de negócio é violada")
public record ErroResponse(
        @Schema(description = "Tipo do erro", example = "Regra de Negócio Violada")
        String erro,

        @Schema(description = "Descrição detalhada do erro", example = "Uma ocorrência resolvida não pode mudar de status.")
        String mensagem
) {}

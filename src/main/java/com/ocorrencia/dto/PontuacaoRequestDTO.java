package com.ocorrencia.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PontuacaoRequestDTO {

    private Long usuarioId;
    private Integer pontos;
    private String descricao;

    public PontuacaoRequestDTO(Long usuarioId, Integer pontos, String descricao) {
        this.usuarioId = usuarioId;
        this.pontos = pontos;
        this.descricao = descricao;
    }
}

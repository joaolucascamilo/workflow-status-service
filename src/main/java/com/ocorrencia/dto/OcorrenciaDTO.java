package com.ocorrencia.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OcorrenciaDTO {
    private Long id;
    private Long usuarioId;

    public OcorrenciaDTO(Long id, Long usuarioId) {
        this.id = id;
        this.usuarioId = usuarioId;
    }


}

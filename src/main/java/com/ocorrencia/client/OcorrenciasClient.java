package com.ocorrencia.client;

import com.ocorrencia.dto.OcorrenciaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "ms-ocorrencias", url = "${ms-ocorrencias.url}")
public interface OcorrenciasClient {

    @GetMapping("/api/ocorrencias/{id}")
    OcorrenciaDTO buscarPorId(@PathVariable("id") Long id);

    @PutMapping("/api/ocorrencias/{id}/status")
    void atualizarStatusOcorrencia(
            @PathVariable("id") Long id,
            @RequestBody Integer statusCodigo,
            @RequestHeader("Authorization") String token
    );
}

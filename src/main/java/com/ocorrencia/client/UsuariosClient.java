package com.ocorrencia.client;

import com.ocorrencia.dto.PontuacaoRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-usuarios", url = "http://localhost:8082")
public interface UsuariosClient {

    @PostMapping("/api/usuarios/pontuar")
    void adicionarPontos(@RequestBody PontuacaoRequestDTO request);
}

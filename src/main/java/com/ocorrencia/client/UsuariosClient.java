package com.ocorrencia.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "ms-usuarios", url = "http://localhost:8082") // Ajuste a porta do seu MS 5
public interface UsuariosClient {

    @PostMapping("/api/usuarios/{usuarioId}/adicionarPonto")
    void adicionarPontos(@PathVariable("usuarioId") Long usuarioId);
}

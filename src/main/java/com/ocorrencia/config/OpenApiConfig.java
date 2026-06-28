package com.ocorrencia.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI workflowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Workflow Status Service API")
                        .description("""
                                Microsserviço responsável por gerenciar o ciclo de vida de status das ocorrências urbanas.

                                **Regras de transição:**
                                - Toda ocorrência começa sem status (nulo) e pode receber qualquer estado inicial.
                                - Uma ocorrência com status **RESOLVIDO** não pode mais ser alterada.
                                - Não é permitido atualizar para o mesmo status atual.

                                **Integração:**
                                - Sincroniza mudanças de status com o serviço de ocorrências (`ms-ocorrencias`).
                                - Ao resolver uma ocorrência, concede 50 pontos ao usuário via serviço de usuários (`ms-usuarios`).
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("TCC - Sistema de Gestão de Ocorrências")
                                .email("lucascamilo373@gmail.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8083").description("Desenvolvimento local")
                ));
    }
}

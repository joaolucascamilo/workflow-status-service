# Workflow Status Service

Microsserviço responsável por gerenciar o ciclo de vida e o histórico de status de ocorrências em um sistema distribuído de gestão de incidentes.

## Visão Geral

O **workflow-status-service** controla as transições de status das ocorrências, mantém um histórico completo de cada mudança, sincroniza o estado com o serviço de ocorrências e dispara eventos de gamificação quando uma ocorrência é resolvida.

Este serviço faz parte de uma arquitetura de microsserviços composta por:

| Serviço                 | Porta | Responsabilidade                      |
|-------------------------|-------|---------------------------------------|
| ms-ocorrencias          | 8081  | Cadastro e consulta de ocorrências    |
| ms-usuarios             | 8082  | Gestão de usuários e pontuação        |
| workflow-status-service | 8083  | Ciclo de vida e histórico de status   |

## Tecnologias

- **Java 17**
- **Spring Boot 3.2.5**
- **Spring Cloud 2023.0.1** (OpenFeign)
- **Spring Data JPA**
- **PostgreSQL**
- **Lombok**
- **SpringDoc OpenAPI / Swagger UI**
- **JUnit 5 + Mockito**
- **Maven**

## Pré-requisitos

- JDK 17+
- Maven 3.x
- PostgreSQL rodando em `localhost:5432`
- `ms-ocorrencias` rodando em `http://localhost:8081`
- `ms-usuarios` rodando em `http://localhost:8082`

## Configuração

As configurações ficam em `src/main/resources/application.properties`:

```properties
spring.application.name=workflow-status-service
server.port=8083

spring.datasource.url=jdbc:postgresql://localhost:5432/workflow_db
spring.datasource.username=admin
spring.datasource.password=password123

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# URLs dos microsservicos consumidos via Feign
ms-ocorrencias.url=${MS_OCORRENCIAS_URL:http://localhost:8081}
ms-usuarios.url=${MS_USUARIOS_URL:http://localhost:8082}
```

As URLs de `ms-ocorrencias` e `ms-usuarios` podem ser sobrescritas em produção através das variáveis de ambiente `MS_OCORRENCIAS_URL` e `MS_USUARIOS_URL`.

Crie o banco de dados antes de iniciar o serviço:

```sql
CREATE DATABASE workflow_db;
```

O Hibernate cria automaticamente a tabela `historico_workflow` na primeira execução.

### CORS

O serviço libera acesso via CORS (`CorsConfig`) para os seguintes origins, com suporte a `GET`, `POST`, `PUT`, `DELETE` e `OPTIONS`:

- `http://localhost:5500`
- `http://127.0.0.1:5500`
- `https://somar.up.railway.app`

## Como Executar

```bash
# Compilar e executar
mvn spring-boot:run

# Ou gerar o JAR e executar
mvn clean package
java -jar target/workflow-status-service-0.0.1-SNAPSHOT.jar
```

O serviço estará disponível em `http://localhost:8083`.

## Documentação da API (Swagger)

Com o serviço em execução, a documentação interativa gerada pelo SpringDoc OpenAPI fica disponível em:

- Swagger UI: `http://localhost:8083/swagger-ui.html`
- Especificação OpenAPI (JSON): `http://localhost:8083/v3/api-docs`

## Endpoints

### Atualizar status de uma ocorrência

```
POST /api/workflow/{ocorrenciaId}/status
```

| Parâmetro     | Tipo    | Onde   | Obrigatório | Descrição                  |
|---------------|---------|--------|-------------|----------------------------|
| ocorrenciaId  | Long    | Path   | Sim         | ID da ocorrência           |
| status        | Integer | Query  | Sim         | Código do novo status      |
| observacao    | String  | Query  | Não         | Observação sobre a mudança |
| Authorization | String  | Header | Sim         | Token Bearer               |

**Exemplo:**
```bash
curl -X POST "http://localhost:8083/api/workflow/1/status?status=3&observacao=Problema+resolvido" \
  -H "Authorization: Bearer <token>"
```

### Consultar status atual de uma ocorrência

```
GET /api/workflow/{ocorrenciaId}/atual
```

| Parâmetro    | Tipo | Onde | Descrição        |
|--------------|------|------|------------------|
| ocorrenciaId | Long | Path | ID da ocorrência |

**Exemplo:**
```bash
curl http://localhost:8083/api/workflow/1/atual
```

## Status da Ocorrência

| Código | Nome            | Descrição             |
|--------|-----------------|-----------------------|
| 1      | REGISTRADO      | Ocorrência registrada |
| 2      | CANCELADO       | Ocorrência cancelada  |
| 3      | RESOLVIDO       | Ocorrência resolvida  |
| 4      | EM_PROCEDIMENTO | Em andamento          |

## Regras de Negócio

- A ocorrência deve existir no `ms-ocorrencias` para ter seu status atualizado.
- Não é permitido atualizar para o mesmo status atual.
- Ocorrências com status **RESOLVIDO** não podem ter o status alterado (estado final imutável).
- Ao resolver uma ocorrência, o serviço dispara automaticamente um evento de gamificação que adiciona **50 pontos** ao usuário responsável via `ms-usuarios`.

## Tratamento de Erros

Violações de regras de negócio retornam HTTP `400 Bad Request` com o seguinte formato:

```json
{
  "erro": "Regra de Negócio Violada",
  "mensagem": "Descrição do problema"
}
```

## Testes

```bash
mvn test
```

Os testes cobrem:
- Carregamento do contexto da aplicação
- Lógica de atualização de status com mocks dos clientes Feign
- Disparo do evento de gamificação ao resolver uma ocorrência

## Estrutura do Projeto

```
src/main/java/com/ocorrencia/
├── WorkflowStatusServiceApplication.java
├── config/
│   ├── CorsConfig.java
│   └── OpenApiConfig.java
├── controller/
│   └── WorkflowController.java
├── service/
│   └── WorkflowService.java
├── entity/
│   └── HistoricoWorkflow.java
├── domain/
│   └── StatusOcorrencia.java
├── dto/
│   ├── ErroResponse.java
│   ├── OcorrenciaDTO.java
│   └── PontuacaoRequestDTO.java
├── client/
│   ├── OcorrenciasClient.java
│   └── UsuariosClient.java
├── repository/
│   └── HistoricoWorkflowRepository.java
└── exception/
    └── GlobalExceptionHandler.java
```
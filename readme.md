# AutoHub - Sales API

API responsável pelo gerenciamento do ciclo de vida das vendas na plataforma AutoHub, incluindo o início da saga de
venda, o armazenamento dos dados da transação e a atualização do status com base nos eventos recebidos de outros
serviços.

## Integrantes:

- Victor Leite RM354905

## Índice

* [Visão Geral](#visão-geral)
* [Arquitetura](#arquitetura)
* [Tecnologias](#tecnologias)
* [Configuração](#configuração)
    * [Variáveis de Ambiente](#variáveis-de-ambiente)
    * [Ficheiros de Configuração](#ficheiros-de-configuração)
* [Executando Localmente](#executando-localmente)
    * [Com Docker Compose](#com-docker-compose)
* [Testes](#testes)
    * [Testes Locais End-to-End](#testes-locais-end-to-end)
* [API Endpoints (HTTP)](#api-endpoints-http)
* [Eventos Consumidos (SQS)](#eventos-consumidos-sqs)
* [Eventos Publicados (SNS)](#eventos-publicados-sns)
* [Modelo de Dados](#modelo-de-dados)
* [Deployment (AWS Lambda)](#deployment-aws-lambda)

## Visão Geral

Esta API orquestra o início e acompanha o progresso da saga de venda de veículos. Suas principais responsabilidades são:

* Receber a requisição para iniciar uma nova venda (`POST /sales`).
* Validar os dados da requisição.
* Criar um registo inicial na sua base de dados (`sales`) com o status `PENDING_RESERVATION`.
* Publicar o evento `SaleCreated` para que outras APIs (principalmente `vehicles-api`) possam iniciar seus processos.
* Consumir eventos de resultado das outras etapas da saga (`VehicleReservationFailed`, `PaymentCompleted`,
  `PaymentFailed`, `ChargeCreationFailed`, `ChargeExpired`).
* Atualizar o status final da venda (`COMPLETED`, `FAILED`, `RESERVATION_FAILED`, etc.) na sua base de dados com base
  nos eventos recebidos.
* Manter um log de auditoria das mudanças de estado da venda (`sales_audit_log`).

## Arquitetura

A API segue a **Arquitetura Hexagonal**, separando o domínio de negócio da infraestrutura.

* **Domínio:** Contém as entidades (`Sale`, `SaleStatus`), evento publicado (`SaleCreated`), eventos consumidos,
  exceções e as interfaces das portas (entrada: `SaleServicePort`; saída: `SaleRepositoryPort`,
  `SaleEventPublisherPort`).
* **Aplicação:** Contém a implementação da lógica de negócio (`SaleServiceImpl`).
* **Infraestrutura:** Contém os adaptadores:
    * **Entrada:** Controller REST (`SaleController`); Consumidor SQS (`SaleEventConsumer`) para eventos da saga.
    * **Saída:** Adaptador de persistência para PostgreSQL (`PostgresSaleRepositoryAdapter`), Adaptador de publicação
      para SNS (`SnsSaleEventPublisherAdapter`).
* **Deployment:** A aplicação é empacotada como um "fat JAR" e deployada em duas funções AWS Lambda distintas:
    * **Lambda HTTP:** Acionada pelo API Gateway, usa `StreamLambdaHandler` (`aws-serverless-java-container`).
      Responsável pelos endpoints REST.
    * **Lambda SQS:** Acionada por Event Source Mapping de uma fila SQS unificada, usa `FunctionInvoker` (
      `spring-cloud-function-adapter-aws`). Responsável por processar eventos da saga (`VehicleReservationFailed`,
      eventos de pagamento, etc.).

## Tecnologias

* **Linguagem:** Java 21
* **Framework:** Spring Boot 3.4.4
* **Build:** Maven
* **Base de Dados:** AWS RDS PostgreSQL
* **Mensageria:** AWS SNS, AWS SQS (via Spring Cloud AWS / Spring Cloud Function)
* **Infraestrutura:** AWS Lambda, API Gateway, Terraform, AWS Secrets Manager
* **Testes:** JUnit 5, Mockito, Testcontainers (PostgreSQL, LocalStack)
* **Documentação:** Springdoc OpenAPI (Swagger UI)
* **Outros:** MapStruct, Spring Data JPA, Hibernate, Flyway (para gestão do schema DB)

## Configuração

A configuração da aplicação é gerenciada através de perfis Spring e ficheiros `application*.yml`.

### Variáveis de Ambiente

As seguintes variáveis de ambiente são esperadas, especialmente no ambiente AWS (configuradas via Terraform):

* `SPRING_PROFILES_ACTIVE`: Define os perfis ativos (ex: `prod,http` ou `prod,sqs`).
* `AWS_REGION`: Região AWS onde a aplicação está a correr.
* `DB_HOST`: Endpoint do RDS PostgreSQL para vendas.
* `DB_PORT`: Porta do RDS PostgreSQL para vendas.
* `DB_NAME`: Nome do banco de dados no RDS de vendas.
* `DB_USER`: Usuário master do RDS de vendas.
* `DB_PASSWORD_SECRET_ARN`: ARN do segredo no Secrets Manager contendo a senha do DB de vendas.
* `SNS_TOPIC_MAIN_EVENT_BUS_ARN`: ARN do tópico SNS principal.
* `SQS_QUEUE_SALES_EVENTS_NAME`: Nome da fila SQS unificada para eventos da Sales API.
* `SPRING_CLOUD_FUNCTION_DEFINITION`: (Apenas para Lambda SQS) Nome do bean `@Bean Consumer<SQSEvent>` (ex:
  `saleEventsConsumer`).
* `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`: (Apenas para Lambda HTTP) URI do emissor JWT para validação de
  token.

### Ficheiros de Configuração

* `application.yml`: Configurações base, defaults para ambiente local, placeholders.
* `application-prod.yml`: Configurações comuns de produção (JPA, Flyway desabilitado, região AWS, nível de log). Define
  as propriedades AWS/DB para ler das variáveis de ambiente.
* `application-http.yml`: Ativado com perfil `http`. Exclui auto-configurações SQS/Function. Define
  `spring.flyway.enabled: false`.
* `application-sqs.yml`: Ativado com perfil `sqs`. Define `web-application-type: none`, exclui auto-configurações
  Web/Security/Swagger, define `spring.cloud.function.definition`. Define `spring.flyway.enabled: false`.
* `application-local.yml`: Ativado com perfil `local`. Aponta para endpoints do LocalStack e Postgres do Docker Compose,
  define credenciais dummy, nomes/URLs locais. Define `spring.jpa.hibernate.ddl-auto: update` e
  `spring.flyway.enabled: false`.
* `application-test.yml`: Ativado com perfil `test`. Exclui `SecretsManager`, configura DataSource H2 (ou para
  Testcontainers), desabilita Flyway, define `issuer-uri` dummy.

## Executando Localmente

### Com Docker Compose

1. Certifique-se de que Docker e Docker Compose estão correndo.
2. Navegue até ao diretório que contém o `docker-compose.yml` (que deve incluir serviços para Postgres (
   `postgres-sales-local`) e LocalStack).
3. Inicie os serviços: `docker-compose up -d`. O script em localstack-init/init-aws.sh já deverá criar toda a
   infraestrutura no docker.
4. Inicie a aplicação Spring Boot com os perfis apropriados:
    * Para testar a API/Swagger: `-Dspring.profiles.active=local,http`
    * Para testar o consumidor SQS: `-Dspring.profiles.active=local,sqs`

## Testes

### Testes Locais End-to-End

1. Inicie a aplicação com perfis `local,http`.
2. Use o Swagger UI (`http://localhost:8080/swagger-ui.html`) ou `curl` para chamar `POST /sales`. Use um JWT de teste.
3. Verifique os logs, a criação do registo na tabela `sales` no Postgres local e a publicação do evento `SaleCreated` no
   SNS do LocalStack.
4. Inicie a aplicação com perfis `local,sqs`.
5. Use `awslocal sqs send-message` para enviar eventos simulados (`VehicleReservationFailed`, `PaymentCompleted`, etc.)
   para a fila `SalesApi_Events_Queue-local`.
6. Verifique os logs da aplicação e a atualização do status na tabela `sales` no Postgres local.

## API Endpoints (HTTP)

* **Swagger UI:** `http://localhost:8080/swagger-ui.html` (quando a correr com perfil `http`)

| Método | Path                  | Autenticação | Descrição                 |
|:-------|:----------------------|:-------------|:--------------------------|
| POST   | `/sales`              | JWT Bearer   | Inicia uma nova venda.    |
| GET    | `/sales/{id}`         | JWT Bearer   | Busca uma venda por ID.   |
| GET    | `/sales/my-purchases` | JWT Bearer   | Lista compras do usuário. |
| GET    | `/sales/my-sales`     | JWT Bearer   | Lista vendas do usuário.  |

## Eventos Consumidos (SQS)

A Lambda SQS (`AutoHubSalesApiSqs-{env}`) consome da fila unificada `SalesApi_Events_Queue-{env}`:

| EventType                  | Publicado Por                            | Descrição                     | Ação na Sales API                                        |
|:---------------------------|:-----------------------------------------|:------------------------------|:---------------------------------------------------------|
| `VehicleReservationFailed` | `vehicles-api` (via SNS)                 | Falha na reserva do veículo.  | Atualiza status da venda para `RESERVATION_FAILED`.      |
| `ChargeCreationFailed`     | `charges-api` (via SNS)                  | Falha na criação da cobrança. | Atualiza status da venda para `FAILED`.                  |
| `PaymentCompleted`         | `charges-api` (via SNS)                  | Pagamento confirmado.         | Atualiza status da venda para `COMPLETED` (após DETRAN). |
| `PaymentFailed`            | `charges-api` (via SNS)                  | Pagamento falhou.             | Atualiza status da venda para `PAYMENT_FAILED`.          |
| `ChargeExpired`            | `charges-api`/`timeout-lambda` (via SNS) | Cobrança expirou.             | Atualiza status da venda para `PAYMENT_EXPIRED`.         |

## Eventos Publicados (SNS)

Esta API publica os seguintes eventos no tópico SNS `AutoHubBusinessEventsTopic-{env}`:

| EventType     | Disparado Por                | Descrição               |
|:--------------|:-----------------------------|:------------------------|
| `SaleCreated` | Sucesso na criação da venda. | Inicia a saga de venda. |

## Modelo de Dados

* **Base de Dados:** AWS RDS PostgreSQL
* **Tabelas Principais:**
    * `sales`: Armazena os dados das vendas (id, vehicle_id, buyer_user_id, seller_user_id, price, status,
      failure_reason, charge_id, detran_process_id, version, created_at, updated_at).
    * `sales_audit_log`: Guarda snapshots JSON do histórico de alterações das vendas.
* **Migrações:** Gerenciadas via Flyway (scripts em `src/main/resources/db/migration`).

## Deployment (AWS Lambda)

* **Deploy:** Realizado via pipeline GitHub Actions (`.github/workflows/cicd-sales.yml`).
* **Artefacto:** Um único "fat JAR" com classifier `-aws.jar` gerado pelo `maven-shade-plugin`.
* **Funções:**
    * `AutoHubSalesApiHttp-{env}`:
        * **Trigger:** API Gateway.
        * **Handler:** `com.fiap.autohub.autohub_sales_api.application.config.StreamLambdaHandler`.
        * **Perfis Ativos:** `prod,http`.
    * `AutoHubSalesApiSqs-{env}`:
        * **Trigger:** Event Source Mapping da fila `SalesApi_Events_Queue-{env}`.
        * **Handler:** `org.springframework.cloud.function.adapter.aws.FunctionInvoker`.
        * **Perfis Ativos:** `prod,sqs`.
        * **Variável `SPRING_CLOUD_FUNCTION_DEFINITION`:** `saleEventsConsumer` (ou o nome real do seu bean).
* **Variáveis de Ambiente:** Consultar a seção [Variáveis de Ambiente](#variáveis-de-ambiente).


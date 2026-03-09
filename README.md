# Task API
Criação de uma API Restful para cadastro de tarefas pertencentes a um usuário existente.
Projeto criado para estudo e prática de criação de uma API.

---
## Tecnologias e Ferramentas
- **Backend**: Java 21, Spring Boot 4.0.2, Spring Data JPA, Hibernate, Docker, PostgreSQL.
- **Build**: Maven 4.0.0
- **Testes** JUnit5, Mockito, MockMVC, banco H2.
- **Documentação**: Swagger.

---
## Requisitos

- Java 21
- Maven 4
- Docker (contém as imagens do Java, PostgreSQL e Maven, configuradas para o projeto).

---
## Testes
- **Teste de Integração**: Para validar o fluxo completo (Controller -> Service -> Repository), utilizando o banco H2 para garantir a integridade das respostas.
- **Teste de Serviço**: Para validar a regra de negócio.
- **Testes de Controller**: Para validar o comportamento dos endpoints.
---
## Passo a passo

1. Clone o repositório:
```git
git clone <https://github.com/jose-wolf/task_api.git>
```
2. Na raiz do projeto execute o comando:
```docker
docker compose up
```
Para ver os Logs no terminal. Para encerrar use `Ctrl + C`.   

ou
```docker
docker compose up -d
```
Para encerrar use ``docker compose down``. Irá remover os containers criados.

---
## Documentação da API

- Após subir os containers, acesse: <http://localhost:8080/swagger-ui/index.html> .


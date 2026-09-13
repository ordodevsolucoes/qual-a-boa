# ADR 0002 - JWT com jjwt, erro RFC 9457 com ProblemDetail e autorizacao no servico

Data: 12/09/2026. Situacao: aceita.

## Contexto

A entrega 1 exige autenticação com JWT HS256, tratamento de erro no formato RFC 9457 em
toda a API, e verificação de papel e titularidade (RN01 e RN09) na camada de serviço,
mantendo os serviços testáveis sem contexto Spring.

## Decisao

- JWT: `io.jsonwebtoken:jjwt-api/impl/jackson` 0.12.6. Nao ha suporte a JWT no
  `spring-boot-starter-security` puro sem OAuth2 Resource Server, e o escopo pede algo
  minimo (HS256, secret simetrico), sem servidor de autorizacao.
- Erro RFC 9457: `org.springframework.http.ProblemDetail`, nativo do Spring Framework desde
  a versao 6, dispensa classe propria de corpo de erro.
- Contrato de autorizacao: criei o tipo `UsuarioAutenticado` (id, papel) como principal da
  autenticacao. `EventoService` e `LoteIngressoService` recebem esse tipo como parametro
  explicito nos metodos que alteram ou consultam recurso de um titular, em vez de ler
  `SecurityContextHolder` diretamente. Isso mantem os servicos testaveis com JUnit puro
  (sem contexto Spring) e antecipa, no bloco 2, a assinatura que o bloco 4 vai preencher de
  verdade.
- Como o filtro JWT so existe no bloco 3, os testes de controller do bloco 2 fixam o
  `SecurityContext` manualmente (via `SecurityMockMvcRequestPostProcessors.authentication`)
  para simular um usuario autenticado, sem depender do filtro real.

## Consequencias

O acoplamento a `SecurityContextHolder` fica isolado no controller (que extrai o principal
da requisicao) e nunca aparece dentro de `EventoService`/`LoteIngressoService`. Havera um
refactor pontual da assinatura de `EventoService.criar/publicar/buscarPorId/listarPublicados`
para incluir `UsuarioAutenticado`, com os testes de servico existentes ajustados. `ProblemDetail`
evita reinventar um DTO de erro e ja serializa como `application/problem+json`.

# Resumo da sessao autonoma - 12/09/2026

Sessao sem supervisao, quatro blocos executados em sequencia, todos com `./mvnw verify`
verde ao final. Nenhum push foi feito; os commits ficaram locais na branch
`feat/esquema-e-dominio` para revisao.

## Bloco 1 - RN05 a RN08 (regras do lote)

Concluido. `LoteIngressoService` criado do zero, teste antes da implementacao em cada RN:

- RN05: recusa lote sem nome, com quantidade zero ou preco negativo, ou sem vigencia.
- RN06: recusa vigencia que ultrapassa o inicio do evento (fronteira: terminar exatamente no
  inicio do evento e aceito; um segundo depois e recusado).
- RN07: recusa lote que excede a capacidade do evento, informando a folga restante na
  mensagem (fronteira: soma igual a capacidade e aceita; um a mais e recusado).
- RN08: recusa vigencia sobreposta a outro lote do mesmo evento (fronteira: lotes encostados,
  onde um comeca exatamente quando o outro termina, sao aceitos; sobreposicao parcial,
  contencao total e inicio identico sao recusados).

4 commits, um por RN. `LoteIngressoServiceTest` com 14 casos.

## Bloco 2 - camada REST e erro RFC 9457

Concluido. DTOs (`EventoRequest`/`EventoResponse`/`LoteIngressoRequest`/`LoteIngressoResponse`)
separados das entidades, mapeados com MapStruct; Bean Validation em todo DTO de entrada.
Endpoints implementados exatamente como especificado (`POST/GET/PUT /api/v1/eventos`,
`POST /api/v1/eventos/{id}/lotes`, `POST /api/v1/eventos/{id}/publicacao`).

`ManipuladorDeExcecoes` (`@RestControllerAdvice`) usa `org.springframework.http.ProblemDetail`
(nativo do Spring, RFC 9457) para todo erro: `RegraDeNegocioException` -> 422 com o codigo da
regra no corpo, `MethodArgumentNotValidException` -> 400 com array `errors` por campo,
`OptimisticLockingFailureException` -> 409, qualquer outra -> 500 generico sem stacktrace,
nome de classe, SQL ou mensagem de framework. Teste dedicado verifica a ausencia desses
termos no corpo da resposta 400.

`EventoService` foi refatorado para receber `UsuarioAutenticado` (id + papel) como parametro
explicito, em vez de ler `SecurityContextHolder` direto no servico - decisao registrada na
ADR 0002. `EventoControllerTest#devolve400ComDetalhePorCampo` criado com o nome exato da
issue RN02 (a issue prescreve um teste de controller; a validacao de negocio em si continua
testada em `EventoServiceTest` com nome proprio, decisao da sessao anterior).

swagger-ui e api-docs liberados sem autenticacao.

## Bloco 3 - autenticacao minima

Concluido. `POST /api/v1/auth/login` valida e-mail/senha com `BCryptPasswordEncoder` contra
`usuario`, devolve JWT HS256 (biblioteca `io.jsonwebtoken:jjwt`, ADR 0002) com o id no
subject, papel como claim, expiracao em 30 minutos. `JWT_SECRET` sem valor padrao; ausente
ou com menos de 32 bytes faz a aplicacao falhar ao subir com mensagem clara
(`ConfiguracaoJwtTest`, 3 casos). `JwtAuthenticationFilter` popula o `SecurityContext` a
partir do token; `SecurityFilterChain` ficou stateless, com `/api/v1/auth/login` liberado.
Credencial invalida devolve 401 com a mesma mensagem para e-mail inexistente e senha errada
(`AuthServiceTest`, `AuthControllerTest`).

Verificacao de log: nenhum ponto do codigo (novo ou pre-existente) loga senha, token ou o
cabecalho `Authorization`. O usuario em memoria com senha gerada que o Spring Boot cria por
padrao (e loga no console) foi desligado com um `InMemoryUserDetailsManager` vazio.

Durante o TDD do `JwtServiceTest`, o teste de expiracao falhou porque o parser do jjwt valida
"exp" contra o relogio real da maquina por padrao, nao contra o `Clock` injetado no servico -
corrigido explicitando `Jwts.parser().clock(...)` com o mesmo `Clock`, para manter o teste
deterministico.

## Bloco 4 - RN01 e RN09 (controle de acesso)

Concluido. Verificacao no servico, nao so por anotacao: `EventoService` recusa com
`AcessoNegadoException` (403) quando o papel do usuario autenticado nao e LOCAL_DE_CURSO, ou
quando o evento pedido nao pertence a ele - as duas situacoes (evento de outro titular e
evento inexistente) usam a mesma excecao e a mesma mensagem, de proposito, para as respostas
serem indistinguiveis (RN09). `LoteIngressoService` nao precisou de verificacao propria:
toda criacao de lote passa primeiro por `EventoService.buscarPorId`, que ja garante papel e
titularidade.

RN01 foi estendida, por decisao registrada na ADR 0003, para cobrir todo endpoint de evento
(inclusive GET), nao so criacao/edicao/publicacao como o texto literal da regra: nesta
entrega nao existe nenhum caso de uso de PARTICIPANTE sobre evento, e a tarefa pedia a mesma
matriz de teste (401/403/403/sucesso) para "cada endpoint protegido".

Testes com nome exato das issues: `EventoServiceTest` ja cobria RN09 desde o bloco 2;
`EventoControllerTest#recusaPublicacaoDeParticipanteCom403` (RN01) e
`EventoControllerTest#recusaAcessoAEventoDeOutroTitularCom403` (RN09) foram acrescentados
neste bloco, junto da matriz completa (sem token 401, papel errado 403, titular errado 403,
titular correto sucesso).

## Cobertura (JaCoCo, `target/site/jacoco/index.html`)

- Instrucoes: 91% (1.678 de 1.832 cobertas, 154 nao cobertas)
- Linhas: 91,7% (385 de 420 cobertas, 35 nao cobertas)
- Ramos (branches): 77% (48 de 62 cobertos, 14 nao cobertos)

Pacotes mais baixos: `br.com.ordodev.qualaboa.seguranca` (65% instrucoes) - o ramo de
exececao do `JwtAuthenticationFilter` para token malformado nao tem teste dedicado ainda; e
`br.com.ordodev.qualaboa` (37%, e so a classe `QualABoaApiApplication`, cujo `main` nao e
exercitado por teste de unidade).

## Testes com @Disabled

Nenhum. Nenhuma implementacao desta sessao ficou bloqueada apos tres tentativas.

## Decisoes tomadas sem consulta (alem das registradas em ADR)

- `EventoRequest` e reaproveitado tanto para `POST` quanto para `PUT`: o formato exigido e
  identico (substituicao completa), criar um DTO por operacao so duplicaria as mesmas
  validacoes.
- `AuthControllerTest` usa `@AutoConfigureMockMvc(addFilters = false)`: o alvo desse teste e
  o contrato do controller (delegar para `AuthService`, formatar a resposta), nao a politica
  de autorizacao, que tem sua propria suite no bloco 4.
- RN03 (data de inicio nao pode ser anterior ao instante da criacao) foi aplicada com a
  mesma regra tanto na criacao quanto na edicao do evento (`PUT`): nao ha, no texto da regra,
  distincao entre os dois casos, e permitir editar um evento para uma data no passado
  contradiria o proposito da regra.

## Comandos para reproduzir

```bash
cd backend
docker compose up -d   # a partir da raiz do repo
DB_URL=jdbc:postgresql://localhost:5432/qualaboa DB_USER=qualaboa DB_PASSWORD=qualaboa \
  JWT_SECRET=<32-bytes-ou-mais> CORS_ORIGENS=http://localhost:5173 ./mvnw verify
```

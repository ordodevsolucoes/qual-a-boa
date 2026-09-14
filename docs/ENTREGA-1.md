# Entrega 1 - folha de evidencias (14/09/2026)

Escopo: RN01 a RN09, publicacao de evento com lotes de ingressos (Ficha, item 7.1.1).

| RN | Issue | Pull Request | Teste automatizado | Situacao |
|---|---|---|---|---|
| RN01 | [#1](https://github.com/ordodevsolucoes/qual-a-boa/issues/1) | [#15](https://github.com/ordodevsolucoes/qual-a-boa/pull/15) | `EventoServiceTest.recusaCriacaoQuandoPapelNaoELocalDeCurso`, `EventoServiceTest.recusaPublicacaoQuandoPapelNaoELocalDeCurso`, `EventoControllerTest.recusaCriacaoDeParticipanteCom403`, `EventoControllerTest.recusaPublicacaoDeParticipanteCom403` | Implementado e testado |
| RN02 | [#2](https://github.com/ordodevsolucoes/qual-a-boa/issues/2) | [#15](https://github.com/ordodevsolucoes/qual-a-boa/pull/15) | `EventoServiceTest.recusaEventoComCampoObrigatorioAusente` (parametrizado, 12 campos) | Implementado e testado |
| RN03 | [#3](https://github.com/ordodevsolucoes/qual-a-boa/issues/3) | [#15](https://github.com/ordodevsolucoes/qual-a-boa/pull/15) | `EventoServiceTest.recusaEventoComDataDeInicioNoPassado`, `EventoServiceTest.recusaEventoComTerminoAnteriorOuIgualAoInicio` | Implementado e testado |
| RN04 | [#4](https://github.com/ordodevsolucoes/qual-a-boa/issues/4) | [#15](https://github.com/ordodevsolucoes/qual-a-boa/pull/15) | `EventoServiceTest.recusaPublicacaoDeEventoSemLote`, `EventoServiceTest.permitePublicacaoDeEventoComLote`, `EventoControllerTest.publicarSemLoteDevolve422ComRegra` | Implementado e testado |
| RN05 | [#5](https://github.com/ordodevsolucoes/qual-a-boa/issues/5) | [#15](https://github.com/ordodevsolucoes/qual-a-boa/pull/15) | `LoteIngressoServiceTest.recusaLoteComQuantidadeZeroOuPrecoNegativo` (parametrizado), `LoteIngressoServiceTest.recusaLoteComCampoObrigatorioAusente` (parametrizado) | Implementado e testado |
| RN06 | [#6](https://github.com/ordodevsolucoes/qual-a-boa/issues/6) | [#15](https://github.com/ordodevsolucoes/qual-a-boa/pull/15) | `LoteIngressoServiceTest.recusaLoteComVigenciaAlemDoInicioDoEvento`, `LoteIngressoServiceTest.recusaLoteComVigenciaFimAnteriorOuIgualAoInicio`, `LoteIngressoServiceTest.aceitaLoteComVigenciaTerminandoNoInicioDoEvento` | Implementado e testado |
| RN07 | [#7](https://github.com/ordodevsolucoes/qual-a-boa/issues/7) | [#15](https://github.com/ordodevsolucoes/qual-a-boa/pull/15) | `LoteIngressoServiceTest.recusaLoteQueExcedeCapacidadeInformandoFolga`, `LoteIngressoServiceTest.aceitaLoteQuandoSomaIgualACapacidade` | Implementado e testado |
| RN08 | [#8](https://github.com/ordodevsolucoes/qual-a-boa/issues/8) | [#15](https://github.com/ordodevsolucoes/qual-a-boa/pull/15) | `LoteIngressoServiceTest.recusaLoteComVigenciaSobreposta`, `LoteIngressoServiceTest.recusaLoteComVigenciaContidaNaOutra`, `LoteIngressoServiceTest.recusaLoteComVigenciaDeInicioIdentico`, `LoteIngressoServiceTest.aceitaLotesComVigenciasEncostadas` | Implementado e testado |
| RN09 | [#9](https://github.com/ordodevsolucoes/qual-a-boa/issues/9) | [#15](https://github.com/ordodevsolucoes/qual-a-boa/pull/15) | `EventoServiceTest.listaApenasEventosDoProprioTitular`, `EventoServiceTest.permiteAcessoAEventoProprio`, `EventoServiceTest.recusaAcessoAEventoDeOutroTitularEAEventoInexistenteComAMesmaMensagem`, `EventoServiceTest.recusaAtualizacaoDeEventoDeOutroTitular`, `EventoControllerTest.recusaAcessoAEventoDeOutroTitularCom403`, `EventoControllerTest.permiteAcessoDeLocalDeCursoAoProprioEvento` | Implementado e testado |

Testes em `backend/src/test/java/br/com/ordodev/qualaboa/evento/EventoServiceTest.java`,
`.../evento/LoteIngressoServiceTest.java` e `.../evento/api/EventoControllerTest.java`.

Nota: o front-end (`frontend/src/telas/FormularioEvento.tsx`) espelha RN02, RN03, RN05, RN06,
RN07 e RN08 no cliente antes de enviar, como validacao de UX — a validacao que vale continua
sendo a do backend, listada acima. Cobertura desse espelhamento em
`frontend/src/__tests__/formularioEvento.test.tsx`: `recusa data de inicio no passado antes
de enviar` e `mostra no campo correto a mensagem de erro vinda do array errors da API`.

## Migracao Flyway

- `V1__esquema_inicial.sql` - entidades `evento` e `lote_ingresso`
- `V2__usuario_semente.sql` - usuarios de teste

## Ambientes

- Front-end publicado: <https://qual-a-boa-eta.vercel.app>
- Back-end: <https://qual-a-boa-api-162191441446.southamerica-east1.run.app>
- Documentacao da API (Swagger): <https://qual-a-boa-api-162191441446.southamerica-east1.run.app/swagger-ui/index.html>

## Cobertura

Relatorio JaCoCo: `backend/target/site/jacoco/index.html`, gerado localmente com
`./mvnw verify` em 14/09/2026 e lido de `backend/target/site/jacoco/jacoco.csv`.

- Instrucoes: 91,6% (1678/1832)
- Branches: 77,4% (48/62)
- Linhas: 91,7% (385/420)
- 63 testes, 0 falhas

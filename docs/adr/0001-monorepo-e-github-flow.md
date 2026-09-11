# ADR 0001 - Monorepo e GitHub Flow

Data: 10/09/2026. Situacao: aceita.

## Contexto

Desenvolvimento individual, entrega quinzenal, exigencia do PFC de um unico repositorio
publico com documentacao acessivel.

## Decisao

Repositorio unico com `backend/` e `frontend/`. Fluxo de branches GitHub Flow: `main`
sempre publicavel, uma branch curta por frente de trabalho, integracao por Pull Request
vinculado a issue. `main` protegida por Pull Request obrigatorio e verificacao de
integracao continua, sem exigencia de revisor.

## Consequencias

A rastreabilidade requisito, issue, branch, commit, Pull Request e teste fica preservada em
um so lugar. Dispensa-se a sobrecarga de `develop`, `release/*` e `hotfix/*` do git-flow,
cujo proposito e coordenar equipes com janelas de release. A exigencia de revisor foi
afastada porque o autor nao pode aprovar o proprio Pull Request; a verificacao automatizada
assume esse papel.

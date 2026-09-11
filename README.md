# Qual a Boa

Plataforma web para divulgacao, venda e distribuicao de ingressos de eventos educacionais
em Mogi das Cruzes. Projeto Final de Curso, Engenharia de Software, Universidade de Mogi
das Cruzes, 2026/2.

Autor: Joao Vitor Fernandes D'Araujo. Orientadora: Profa. Viviane Guimaraes Ribeiro.

## Stack

React 19 e TypeScript 5 sobre Vite. Java 25 e Spring Boot 4.1. PostgreSQL 18 no Supabase,
com migracoes versionadas no Flyway. Front-end publicado na Vercel, back-end em contêiner
no Google Cloud Run.

## Execucao local

```bash
cp .env.example .env          # preencha os valores
docker compose up -d          # sobe o PostgreSQL
cd backend && ./mvnw spring-boot:run
cd frontend && npm install && npm run dev
```

## Entregas

| Entrega | Data | Regras | Escopo |
|---|---|---|---|
| 1 | 14/09/2026 | RN01-RN09 | Publicacao de evento com lotes de ingressos |
| 2 | 28/09/2026 | RN10-RN18 | Acesso, protecao de dados pessoais e trilha de auditoria |
| 3 | 09/11/2026 | RN19-RN29 | Descoberta, venda, emissao, validacao e indicadores |
| 4 | 23/11/2026 | - | Consolidacao de testes, publicacao e documentacao |

Documentacao em `docs/`. Decisoes de arquitetura em `docs/adr/`.

# Qual a Boa

Plataforma web para divulgacao, venda e distribuicao de ingressos de eventos educacionais
em Mogi das Cruzes. Projeto Final de Curso, Engenharia de Software, Universidade de Mogi
das Cruzes, 2026/2.

Autor: Joao Vitor Fernandes D'Araujo. Orientadora: Profa. Viviane Guimaraes Ribeiro.

Nesta primeira entrega, o local de curso (organizador) cria um evento, cadastra os lotes de
ingresso e publica o evento quando ele tiver pelo menos um lote valido. Autenticacao e
autorizacao por papel via JWT.

## Stack

| Camada | Tecnologia | Versao |
|---|---|---|
| Back-end | Java | 25 |
| Back-end | Spring Boot | 4.1.1 |
| Back-end | Flyway | gerenciado pelo Spring Boot 4.1.1 |
| Back-end | MapStruct | 1.6.3 |
| Back-end | springdoc-openapi | 3.1.1 |
| Back-end | JJWT (JSON Web Token) | 0.12.6 |
| Back-end | JaCoCo | 0.8.15 |
| Front-end | React | 19.2.8 |
| Front-end | TypeScript | ~6.0.2 |
| Front-end | Vite | 8.3.0 |
| Front-end | React Router | 7.18.3 |
| Front-end | Zod | 4.6.2 |
| Front-end | Vitest + Testing Library | 5.0.0 / 16.3.3 |
| Banco de dados | PostgreSQL (local, Docker) | 18 |
| Banco de dados | PostgreSQL (Supabase, producao) | 17.6 |
| Publicacao | Google Cloud Run | back-end |
| Publicacao | Vercel | front-end |
| CI | GitHub Actions | Java 25 / Node 22 |

## Execucao local

Testado a partir de um clone limpo.

```bash
git clone https://github.com/ordodevsolucoes/qual-a-boa.git
cd qual-a-boa
docker compose up -d          # sobe o PostgreSQL local em :5432 (usuario/senha/banco: qualaboa)
```

Backend (variaveis de ambiente sao obrigatorias: `application.properties` nao tem default
para banco nem JWT_SECRET; copiar `.env.example` para `.env` nao basta, pois o Spring Boot
nao le arquivo `.env` sozinho — exporte as variaveis, como abaixo):

```bash
cd backend
DB_URL=jdbc:postgresql://localhost:5432/qualaboa \
DB_USER=qualaboa \
DB_PASSWORD=qualaboa \
JWT_SECRET=$(openssl rand -base64 48) \
CORS_ORIGENS=http://localhost:5173 \
./mvnw spring-boot:run
```

Front-end, em outro terminal:

```bash
cd frontend
echo "VITE_API_URL=http://localhost:8080" > .env.local
npm install
npm run dev
```

Acesse `http://localhost:5173` e entre com um dos usuarios semente abaixo.

### Usuarios semente

Criados pela migracao `V2__usuario_semente.sql`.

| E-mail | Senha | Papel |
|---|---|---|
| `local@qualaboa.dev` | `LocalDeCurso@123` | `LOCAL_DE_CURSO` |
| `participante@qualaboa.dev` | `Participante@123` | `PARTICIPANTE` |

Nesta entrega, so `LOCAL_DE_CURSO` acessa os endpoints de evento (RN01).

## Testes e cobertura

```bash
cd backend && ./mvnw verify     # build + 63 testes JUnit + relatorio JaCoCo
cd frontend && npm test         # Vitest + Testing Library
cd frontend && npm run build    # type-check + build de producao
```

Evidencia detalhada por regra de negocio, incluindo nome exato dos metodos de teste, em
[`docs/ENTREGA-1.md`](docs/ENTREGA-1.md).

## Entregas

| Entrega | Data | Regras | Escopo |
|---|---|---|---|
| 1 | 14/09/2026 | RN01-RN09 | Publicacao de evento com lotes de ingressos |
| 2 | 28/09/2026 | RN10-RN18 | Acesso, protecao de dados pessoais e trilha de auditoria |
| 3 | 09/11/2026 | RN19-RN29 | Descoberta, venda, emissao, validacao e indicadores |
| 4 | 23/11/2026 | - | Consolidacao de testes, publicacao e documentacao |

### Fora do escopo desta entrega

Deliberadamente nao implementado em RN01-RN09, por pertencer a uma entrega posterior:

**Entrega 2 (RN10-RN18)**
- Cadastro de usuario
- Refresh token
- Consentimento LGPD
- Recuperacao de senha
- Trilha de auditoria (Hibernate Envers)

**Entrega 3 (RN19-RN29)**
- Catalogo publico de eventos, com filtros e busca
- Compra e pagamento de ingresso
- Emissao e validacao de QR Code
- Dashboard de indicadores
- Upload de imagem do evento
- Pagina de perfil do usuario
- Tema claro (a interface atual e so escura)

Documentacao em `docs/`. Decisoes de arquitetura em `docs/adr/`.

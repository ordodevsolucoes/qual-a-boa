CREATE TABLE usuario (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    email       text NOT NULL UNIQUE,
    senha_hash  text NOT NULL,
    nome        text NOT NULL,
    papel       text NOT NULL,
    criado_em   timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT usuario_papel_valido CHECK (papel IN ('PARTICIPANTE', 'LOCAL_DE_CURSO'))
);

CREATE TABLE evento (
    id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    local_id           uuid NOT NULL REFERENCES usuario (id),
    titulo             text NOT NULL,
    descricao          text NOT NULL,
    area_conhecimento  text NOT NULL,
    inicio             timestamptz NOT NULL,
    termino            timestamptz NOT NULL,
    logradouro         text NOT NULL,
    numero             text NOT NULL,
    bairro             text NOT NULL,
    cidade             text NOT NULL,
    uf                 char(2) NOT NULL,
    cep                char(8) NOT NULL,
    capacidade_total   integer NOT NULL,
    situacao           text NOT NULL DEFAULT 'RASCUNHO',
    -- controle de concorrencia otimista: incrementado a cada UPDATE pelo @Version do JPA
    versao             integer NOT NULL DEFAULT 0,
    criado_em          timestamptz NOT NULL DEFAULT now(),
    atualizado_em      timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT evento_situacao_valida CHECK (situacao IN ('RASCUNHO', 'PUBLICADO')),
    -- RN03: termino do evento sempre posterior ao inicio
    CONSTRAINT evento_termino_apos_inicio CHECK (termino > inicio),
    CONSTRAINT evento_capacidade_positiva CHECK (capacidade_total > 0),
    CONSTRAINT evento_titulo_tamanho_maximo CHECK (char_length(titulo) <= 150),
    CONSTRAINT evento_descricao_tamanho_maximo CHECK (char_length(descricao) <= 2000)
);

CREATE TABLE lote_ingresso (
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    evento_id         uuid NOT NULL REFERENCES evento (id) ON DELETE CASCADE,
    nome              text NOT NULL,
    quantidade        integer NOT NULL,
    preco             numeric(10, 2) NOT NULL,
    vigencia_inicio   timestamptz NOT NULL,
    vigencia_fim      timestamptz NOT NULL,
    versao            integer NOT NULL DEFAULT 0,
    criado_em         timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT lote_quantidade_positiva CHECK (quantidade > 0),
    CONSTRAINT lote_preco_nao_negativo CHECK (preco >= 0),
    -- RN06 depende da vigencia ser um intervalo bem formado
    CONSTRAINT lote_vigencia_fim_apos_inicio CHECK (vigencia_fim > vigencia_inicio)
);

CREATE INDEX idx_evento_local_id ON evento (local_id);
CREATE INDEX idx_evento_situacao ON evento (situacao);
CREATE INDEX idx_lote_ingresso_evento_id ON lote_ingresso (evento_id);

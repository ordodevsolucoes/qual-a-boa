# ADR 0003 - RN01 restringe todo endpoint de evento a LOCAL_DE_CURSO, nao so criacao/edicao/publicacao

Data: 12/09/2026. Situacao: aceita.

## Contexto

O texto da RN01 fala em "criacao, edicao e publicacao". A tarefa pede, para "cada endpoint
protegido" sob `/api/v1/eventos`, a mesma matriz de teste: sem token 401, token de
PARTICIPANTE 403, token de LOCAL_DE_CURSO sobre evento de outro titular 403, token de
LOCAL_DE_CURSO sobre evento proprio sucesso. Isso inclui GET, que a RN01 nao restringe por
papel no texto literal.

## Decisao

Todo endpoint de `/api/v1/eventos/**` (GET incluso) exige papel LOCAL_DE_CURSO, verificado
no servico. Razao: nesta entrega nao existe nenhum caso de uso de PARTICIPANTE sobre evento
(catalogo publico e RN19-RN29, fora de escopo), entao um GET liberado para PARTICIPANTE nao
teria nenhum consumidor real e so complicaria a matriz de teste pedida. RN09 (titularidade)
ja bloqueia PARTICIPANTE indiretamente, porque ele nunca e `local` de nenhum evento; a
verificacao de papel deixa essa restricao explicita e testavel por si so, sem depender desse
efeito colateral.

## Consequencias

Quando a entrega 3 (RN19-RN29) trouxer o catalogo publico, provavelmente havera um novo
endpoint de leitura sem essa restricao (ex.: `GET /api/v1/catalogo`), em vez de afrouxar
este. Ate la, `EventoService` e `LoteIngressoService` continuam sem noção de "leitura
publica" fora de `listarPublicados()`, que nao passa pela verificacao de papel porque nao e
exposto por nenhum controller ainda.

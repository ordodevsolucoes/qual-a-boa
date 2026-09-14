import { useCallback, useEffect, useState } from 'react'
import { Botao } from '../componentes/Botao'
import { Etiqueta } from '../componentes/Etiqueta'
import { useAuth } from '../auth/ContextoAuth'
import { ErroDaApi, requisicao } from '../api/cliente'
import type { EventoResponse } from '../api/tipos'
import { FormularioEvento } from './FormularioEvento'
import './Painel.css'

function formatarContagem(total: number): string {
  return `${total} ${total === 1 ? 'evento' : 'eventos'}`
}

function formatarData(iso: string): string {
  return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
}

export function Painel() {
  const { usuario } = useAuth()

  const [eventos, setEventos] = useState<EventoResponse[] | null>(null)
  const [carregando, setCarregando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const [mostrarFormulario, setMostrarFormulario] = useState(false)
  const [publicandoId, setPublicandoId] = useState<string | null>(null)
  const [erroPorId, setErroPorId] = useState<Record<string, string>>({})

  const carregarEventos = useCallback(async () => {
    setCarregando(true)
    setErro(null)
    try {
      const resposta = await requisicao('/api/v1/eventos')
      const dados = (await resposta.json()) as EventoResponse[]
      setEventos(dados)
    } catch (excecao) {
      setErro(
        excecao instanceof ErroDaApi
          ? excecao.detail
          : 'Nao foi possivel carregar os eventos, verifique sua conexao',
      )
    } finally {
      setCarregando(false)
    }
  }, [])

  useEffect(() => {
    carregarEventos()
  }, [carregarEventos])

  // com o formulario aberto em modal, a pagina de tras fica travada tambem para rolagem,
  // nao so para clique -- o overlay ja cobre a tela e nao tem onClick, entao so o X fecha
  useEffect(() => {
    if (!mostrarFormulario) {
      return
    }
    const overflowOriginal = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    return () => {
      document.body.style.overflow = overflowOriginal
    }
  }, [mostrarFormulario])

  async function publicarRapido(id: string) {
    setPublicandoId(id)
    setErroPorId((atual) => {
      const { [id]: _removido, ...resto } = atual
      return resto
    })
    try {
      await requisicao(`/api/v1/eventos/${id}/publicacao`, { method: 'POST' })
      await carregarEventos()
    } catch (excecao) {
      const mensagem = excecao instanceof ErroDaApi ? excecao.detail : 'Erro inesperado ao publicar'
      setErroPorId((atual) => ({ ...atual, [id]: mensagem }))
    } finally {
      setPublicandoId(null)
    }
  }

  const total = eventos?.length ?? 0
  const publicados = eventos?.filter((evento) => evento.situacao === 'PUBLICADO').length ?? 0
  const vagasOfertadas = eventos?.reduce((soma, evento) => soma + evento.capacidadeTotal, 0) ?? 0

  return (
    <div className="painel">
      <header className="painel__topo">
        <span className="painel__marca">
          QUAL A <span className="painel__marca-acento">BOA</span>
        </span>
        {usuario && <span className="painel__papel">{usuario.papel}</span>}
      </header>

      <div className="painel__cabecalho">
        <div>
          <h1 className="painel__titulo">Meus eventos</h1>
          <p className="painel__contagem">{formatarContagem(total)}</p>
        </div>
        <Botao onClick={() => setMostrarFormulario(true)} disabled={mostrarFormulario}>
          Criar evento
        </Botao>
      </div>

      {mostrarFormulario && (
        <div className="modal-fundo">
          <div className="modal-caixa" role="dialog" aria-modal="true" aria-labelledby="titulo-criar-evento">
            <div className="modal-cabecalho">
              <h2 id="titulo-criar-evento" className="modal-titulo">
                Criar evento
              </h2>
              <button
                type="button"
                className="modal-fechar"
                onClick={() => setMostrarFormulario(false)}
                aria-label="Fechar"
              >
                ×
              </button>
            </div>
            <FormularioEvento
              onCancelar={() => setMostrarFormulario(false)}
              onConcluido={() => {
                setMostrarFormulario(false)
                carregarEventos()
              }}
            />
          </div>
        </div>
      )}

      <div className="painel__cartoes">
        <div className="cartao-numero">
          <p className="cartao-numero__valor">{total}</p>
          <p className="cartao-numero__rotulo">Total de eventos</p>
        </div>
        <div className="cartao-numero">
          <p className="cartao-numero__valor">{publicados}</p>
          <p className="cartao-numero__rotulo">Publicados</p>
        </div>
        <div className="cartao-numero">
          <p className="cartao-numero__valor">{vagasOfertadas}</p>
          <p className="cartao-numero__rotulo">Vagas ofertadas</p>
        </div>
      </div>

      {carregando && eventos === null && <p className="painel__estado">Carregando eventos...</p>}

      {erro && eventos === null && (
        <div className="painel__estado painel__estado--erro">
          <p>{erro}</p>
          <Botao variante="secundario" onClick={carregarEventos}>
            Tentar novamente
          </Botao>
        </div>
      )}

      {eventos !== null && eventos.length === 0 && (
        <div className="painel__vazio">Nenhum evento cadastrado</div>
      )}

      {eventos !== null && eventos.length > 0 && (
        <ul className="painel__lista">
          {eventos.map((evento) => (
            <li key={evento.id} className="cartao-evento">
              <span
                className={
                  'cartao-evento__barra' +
                  (evento.situacao === 'PUBLICADO' ? ' cartao-evento__barra--publicado' : '')
                }
              />
              <div className="cartao-evento__conteudo">
                <p className="cartao-evento__titulo">{evento.titulo}</p>
                <p className="cartao-evento__meta">
                  {formatarData(evento.inicio)} - {evento.cidade}
                </p>
                {erroPorId[evento.id] && <p className="cartao-evento__erro">{erroPorId[evento.id]}</p>}
              </div>
              <Etiqueta situacao={evento.situacao} />
              {evento.situacao === 'RASCUNHO' && (
                <Botao
                  variante="secundario"
                  carregando={publicandoId === evento.id}
                  onClick={() => publicarRapido(evento.id)}
                >
                  Publicar
                </Botao>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

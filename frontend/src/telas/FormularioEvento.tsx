import { useState } from 'react'
import type { ChangeEvent } from 'react'
import { z } from 'zod'
import { Botao } from '../componentes/Botao'
import { Campo } from '../componentes/Campo'
import { ErroDaApi, requisicao } from '../api/cliente'
import type { EventoRequest, EventoResponse, LoteIngressoRequest } from '../api/tipos'
import './FormularioEvento.css'

const AREAS_CONHECIMENTO = ['Tecnologia', 'Ciências', 'Negócios', 'Artes', 'Educação', 'Saúde'] as const

interface CamposEvento {
  titulo: string
  descricao: string
  areaConhecimento: string
  capacidadeTotal: string
  inicio: string
  termino: string
  logradouro: string
  numero: string
  bairro: string
  cidade: string
  uf: string
  cep: string
}

const CAMPOS_VAZIOS: CamposEvento = {
  titulo: '',
  descricao: '',
  areaConhecimento: '',
  capacidadeTotal: '',
  inicio: '',
  termino: '',
  logradouro: '',
  numero: '',
  bairro: '',
  cidade: '',
  uf: '',
  cep: '',
}

interface CamposLote {
  nome: string
  quantidade: string
  preco: string
  vigenciaInicio: string
  vigenciaFim: string
}

const LOTE_VAZIO: CamposLote = {
  nome: '',
  quantidade: '',
  preco: '',
  vigenciaInicio: '',
  vigenciaFim: '',
}

interface LoteAdicionado {
  idLocal: string
  nome: string
  quantidade: number
  preco: number
  vigenciaInicio: string
  vigenciaFim: string
}

// mesmas regras do EventoService (RN02/RN03), verificadas aqui so para dar feedback
// imediato; a validacao real e sempre a que o servidor faz de novo ao receber a requisicao
const schemaEvento = z
  .object({
    titulo: z.string().trim().min(1, 'Campo obrigatorio').max(150, 'Maximo de 150 caracteres'),
    descricao: z.string().trim().min(1, 'Campo obrigatorio').max(2000, 'Maximo de 2000 caracteres'),
    areaConhecimento: z.enum(AREAS_CONHECIMENTO, { message: 'Selecione uma area' }),
    capacidadeTotal: z.coerce.number({ message: 'Campo obrigatorio' }).int('Deve ser um numero inteiro').min(1, 'Minimo de 1 vaga'),
    inicio: z.string().min(1, 'Campo obrigatorio'),
    termino: z.string().min(1, 'Campo obrigatorio'),
    logradouro: z.string().trim().min(1, 'Campo obrigatorio'),
    numero: z.string().trim().min(1, 'Campo obrigatorio'),
    bairro: z.string().trim().min(1, 'Campo obrigatorio'),
    cidade: z.string().trim().min(1, 'Campo obrigatorio'),
    uf: z.string().trim().regex(/^[A-Za-z]{2}$/, 'Deve ter exatamente 2 letras'),
    cep: z.string().trim().regex(/^\d{8}$/, 'Deve ter exatamente 8 digitos'),
  })
  .refine((dados) => new Date(dados.inicio).getTime() >= Date.now(), {
    message: 'Data de inicio nao pode ser anterior ao momento atual',
    path: ['inicio'],
  })
  .refine((dados) => new Date(dados.termino).getTime() > new Date(dados.inicio).getTime(), {
    message: 'Termino deve ser posterior ao inicio',
    path: ['termino'],
  })

// RN05 e a parte de RN06 que nao depende de outros lotes/do evento; o resto (RN06 contra o
// inicio do evento, RN07 contra a capacidade, RN08 contra os lotes ja adicionados) e checado
// na hora de adicionar, porque precisa do estado atual do formulario
const schemaLote = z
  .object({
    nome: z.string().trim().min(1, 'Campo obrigatorio'),
    quantidade: z.coerce.number({ message: 'Campo obrigatorio' }).int('Deve ser um numero inteiro').positive('Deve ser maior que zero'),
    preco: z.coerce.number({ message: 'Campo obrigatorio' }).min(0, 'Nao pode ser negativo'),
    vigenciaInicio: z.string().min(1, 'Campo obrigatorio'),
    vigenciaFim: z.string().min(1, 'Campo obrigatorio'),
  })
  .refine((dados) => new Date(dados.vigenciaFim).getTime() > new Date(dados.vigenciaInicio).getTime(), {
    message: 'Fim da vigencia deve ser posterior ao inicio dela',
    path: ['vigenciaFim'],
  })

function mapearErrosZod(erro: z.ZodError): Record<string, string> {
  const mapa: Record<string, string> = {}
  for (const issue of erro.issues) {
    const campo = issue.path[0]
    if (typeof campo === 'string' && !(campo in mapa)) {
      mapa[campo] = issue.message
    }
  }
  return mapa
}

function formatarPreco(preco: number): string {
  if (preco === 0) {
    return 'Gratuito'
  }
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(preco)
}

interface PropsFormularioEvento {
  onCancelar: () => void
  onConcluido: () => void
}

export function FormularioEvento({ onCancelar, onConcluido }: PropsFormularioEvento) {
  const [campos, setCampos] = useState<CamposEvento>(CAMPOS_VAZIOS)
  const [errosDeCampo, setErrosDeCampo] = useState<Partial<Record<keyof CamposEvento, string>>>({})

  const [novoLote, setNovoLote] = useState<CamposLote>(LOTE_VAZIO)
  const [errosNovoLote, setErrosNovoLote] = useState<Partial<Record<keyof CamposLote, string>>>({})
  const [lotes, setLotes] = useState<LoteAdicionado[]>([])

  const [eventoIdCriado, setEventoIdCriado] = useState<string | null>(null)
  const [lotesCriados, setLotesCriados] = useState<Set<string>>(new Set())
  const [acaoEmAndamento, setAcaoEmAndamento] = useState<'rascunho' | 'publicar' | null>(null)
  const [erroGeral, setErroGeral] = useState<string | null>(null)

  function aoMudarCampo(nome: keyof CamposEvento) {
    return (evento: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
      setCampos((atual) => ({ ...atual, [nome]: evento.target.value }))
    }
  }

  function aoMudarNovoLote(nome: keyof CamposLote) {
    return (evento: ChangeEvent<HTMLInputElement>) => {
      setNovoLote((atual) => ({ ...atual, [nome]: evento.target.value }))
    }
  }

  function validarEvento(): EventoRequest | null {
    const resultado = schemaEvento.safeParse(campos)
    if (!resultado.success) {
      setErrosDeCampo(mapearErrosZod(resultado.error))
      return null
    }
    setErrosDeCampo({})
    const dados = resultado.data
    return {
      titulo: dados.titulo,
      descricao: dados.descricao,
      areaConhecimento: dados.areaConhecimento,
      capacidadeTotal: dados.capacidadeTotal,
      inicio: new Date(dados.inicio).toISOString(),
      termino: new Date(dados.termino).toISOString(),
      logradouro: dados.logradouro,
      numero: dados.numero,
      bairro: dados.bairro,
      cidade: dados.cidade,
      uf: dados.uf.toUpperCase(),
      cep: dados.cep,
    }
  }

  function adicionarLote() {
    const resultado = schemaLote.safeParse(novoLote)
    if (!resultado.success) {
      setErrosNovoLote(mapearErrosZod(resultado.error))
      return
    }
    const dados = resultado.data

    if (campos.inicio && new Date(dados.vigenciaFim).getTime() > new Date(campos.inicio).getTime()) {
      setErrosNovoLote({ vigenciaFim: 'Fim da vigencia deve ser anterior ou igual ao inicio do evento' })
      return
    }

    const capacidade = Number(campos.capacidadeTotal) || 0
    const quantidadeExistente = lotes.reduce((soma, lote) => soma + lote.quantidade, 0)
    if (quantidadeExistente + dados.quantidade > capacidade) {
      const restantes = Math.max(capacidade - quantidadeExistente, 0)
      setErrosNovoLote({ quantidade: `Excede a capacidade do evento, vagas restantes: ${restantes}` })
      return
    }

    const inicioNovo = new Date(dados.vigenciaInicio).getTime()
    const fimNovo = new Date(dados.vigenciaFim).getTime()
    const seSobrepoe = lotes.some((lote) => {
      const inicioExistente = new Date(lote.vigenciaInicio).getTime()
      const fimExistente = new Date(lote.vigenciaFim).getTime()
      return inicioExistente < fimNovo && inicioNovo < fimExistente
    })
    if (seSobrepoe) {
      setErrosNovoLote({ vigenciaInicio: 'Vigencia se sobrepoe a de outro lote ja adicionado' })
      return
    }

    setLotes((atual) => [
      ...atual,
      {
        idLocal: crypto.randomUUID(),
        nome: dados.nome,
        quantidade: dados.quantidade,
        preco: dados.preco,
        vigenciaInicio: dados.vigenciaInicio,
        vigenciaFim: dados.vigenciaFim,
      },
    ])
    setNovoLote(LOTE_VAZIO)
    setErrosNovoLote({})
  }

  function removerLote(idLocal: string) {
    setLotes((atual) => atual.filter((lote) => lote.idLocal !== idLocal))
    setLotesCriados((atual) => {
      const copia = new Set(atual)
      copia.delete(idLocal)
      return copia
    })
  }

  function tratarErroEnvio(excecao: unknown) {
    if (excecao instanceof ErroDaApi && excecao.errors.length > 0) {
      const mapa: Partial<Record<keyof CamposEvento, string>> = {}
      for (const erroDeCampo of excecao.errors) {
        mapa[erroDeCampo.campo as keyof CamposEvento] = erroDeCampo.mensagem
      }
      setErrosDeCampo((atual) => ({ ...atual, ...mapa }))
      return
    }
    setErroGeral(excecao instanceof ErroDaApi ? excecao.detail : 'Erro inesperado ao comunicar com a API')
  }

  async function salvarRascunho() {
    if (eventoIdCriado) {
      onConcluido()
      return
    }
    const dados = validarEvento()
    if (!dados) {
      return
    }
    setAcaoEmAndamento('rascunho')
    setErroGeral(null)
    try {
      await requisicao('/api/v1/eventos', { method: 'POST', body: JSON.stringify(dados) })
      onConcluido()
    } catch (excecao) {
      tratarErroEnvio(excecao)
    } finally {
      setAcaoEmAndamento(null)
    }
  }

  async function publicar() {
    if (lotes.length === 0) {
      return
    }
    const dadosEvento = validarEvento()
    if (!dadosEvento) {
      return
    }
    setAcaoEmAndamento('publicar')
    setErroGeral(null)
    try {
      let id = eventoIdCriado
      if (!id) {
        const respostaEvento = await requisicao('/api/v1/eventos', { method: 'POST', body: JSON.stringify(dadosEvento) })
        const evento = (await respostaEvento.json()) as EventoResponse
        id = evento.id
        setEventoIdCriado(id)
      }

      const criados = new Set(lotesCriados)
      for (const lote of lotes) {
        if (criados.has(lote.idLocal)) {
          continue
        }
        const payload: LoteIngressoRequest = {
          nome: lote.nome,
          quantidade: lote.quantidade,
          preco: lote.preco,
          vigenciaInicio: new Date(lote.vigenciaInicio).toISOString(),
          vigenciaFim: new Date(lote.vigenciaFim).toISOString(),
        }
        await requisicao(`/api/v1/eventos/${id}/lotes`, { method: 'POST', body: JSON.stringify(payload) })
        criados.add(lote.idLocal)
        setLotesCriados(new Set(criados))
      }

      await requisicao(`/api/v1/eventos/${id}/publicacao`, { method: 'POST' })
      onConcluido()
    } catch (excecao) {
      tratarErroEnvio(excecao)
    } finally {
      setAcaoEmAndamento(null)
    }
  }

  const somaQuantidades = lotes.reduce((soma, lote) => soma + lote.quantidade, 0)
  const enviando = acaoEmAndamento !== null

  return (
    <form className="formulario" onSubmit={(evento) => evento.preventDefault()}>
      <div className="formulario__grade">
        <Campo label="Titulo" value={campos.titulo} onChange={aoMudarCampo('titulo')} erro={errosDeCampo.titulo} maxLength={150} />
        <Campo
          label="Descricao"
          como="textarea"
          className="formulario__campo-largo"
          rows={4}
          value={campos.descricao}
          onChange={aoMudarCampo('descricao')}
          erro={errosDeCampo.descricao}
          maxLength={2000}
        />
        <Campo
          label="Area de conhecimento"
          como="select"
          opcoes={AREAS_CONHECIMENTO.map((area) => ({ valor: area, rotulo: area }))}
          value={campos.areaConhecimento}
          onChange={aoMudarCampo('areaConhecimento')}
          erro={errosDeCampo.areaConhecimento}
        />
        <Campo
          label="Capacidade total"
          type="number"
          min={1}
          value={campos.capacidadeTotal}
          onChange={aoMudarCampo('capacidadeTotal')}
          erro={errosDeCampo.capacidadeTotal}
        />
        <Campo
          label="Inicio"
          type="datetime-local"
          value={campos.inicio}
          onChange={aoMudarCampo('inicio')}
          erro={errosDeCampo.inicio}
        />
        <Campo
          label="Termino"
          type="datetime-local"
          value={campos.termino}
          onChange={aoMudarCampo('termino')}
          erro={errosDeCampo.termino}
        />
        <Campo label="Logradouro" value={campos.logradouro} onChange={aoMudarCampo('logradouro')} erro={errosDeCampo.logradouro} />
        <Campo label="Numero" value={campos.numero} onChange={aoMudarCampo('numero')} erro={errosDeCampo.numero} />
        <Campo label="Bairro" value={campos.bairro} onChange={aoMudarCampo('bairro')} erro={errosDeCampo.bairro} />
        <Campo label="Cidade" value={campos.cidade} onChange={aoMudarCampo('cidade')} erro={errosDeCampo.cidade} />
        <Campo label="UF" value={campos.uf} onChange={aoMudarCampo('uf')} erro={errosDeCampo.uf} maxLength={2} />
        <Campo label="CEP" value={campos.cep} onChange={aoMudarCampo('cep')} erro={errosDeCampo.cep} maxLength={8} />
      </div>

      <section className="formulario__lotes">
        <h2 className="formulario__lotes-rotulo">Lotes</h2>

        {lotes.length > 0 && (
          <ul className="formulario__lista-lotes">
            {lotes.map((lote) => (
              <li key={lote.idLocal} className="lote-linha">
                <span className="lote-linha__nome">{lote.nome}</span>
                <span className="lote-linha__quantidade">{lote.quantidade} vagas</span>
                <span className="lote-linha__preco">{formatarPreco(lote.preco)}</span>
                <button
                  type="button"
                  className="lote-linha__remover"
                  onClick={() => removerLote(lote.idLocal)}
                  aria-label={`Remover lote ${lote.nome}`}
                >
                  Remover
                </button>
              </li>
            ))}
          </ul>
        )}

        <p className="formulario__soma-vagas">
          {somaQuantidades} de {campos.capacidadeTotal || 0} vagas
        </p>

        <div className="formulario__linha-adicao">
          <Campo label="Nome" value={novoLote.nome} onChange={aoMudarNovoLote('nome')} erro={errosNovoLote.nome} />
          <Campo
            label="Quantidade"
            type="number"
            min={1}
            value={novoLote.quantidade}
            onChange={aoMudarNovoLote('quantidade')}
            erro={errosNovoLote.quantidade}
          />
          <Campo
            label="Preco"
            type="number"
            min={0}
            step="0.01"
            value={novoLote.preco}
            onChange={aoMudarNovoLote('preco')}
            erro={errosNovoLote.preco}
          />
          <Campo
            label="Vigencia inicio"
            type="datetime-local"
            value={novoLote.vigenciaInicio}
            onChange={aoMudarNovoLote('vigenciaInicio')}
            erro={errosNovoLote.vigenciaInicio}
          />
          <Campo
            label="Vigencia fim"
            type="datetime-local"
            value={novoLote.vigenciaFim}
            onChange={aoMudarNovoLote('vigenciaFim')}
            erro={errosNovoLote.vigenciaFim}
          />
        </div>
        <Botao type="button" variante="secundario" className="formulario__botao-adicionar" onClick={adicionarLote}>
          Adicionar lote
        </Botao>
      </section>

      {erroGeral && <p className="formulario__erro-geral">{erroGeral}</p>}

      <footer className="formulario__acoes">
        {lotes.length === 0 && <p className="formulario__aviso">Publicar exige ao menos um lote válido.</p>}
        <div className="formulario__botoes">
          <Botao type="button" variante="secundario" onClick={onCancelar} disabled={enviando}>
            Cancelar
          </Botao>
          <Botao
            type="button"
            variante="secundario"
            onClick={salvarRascunho}
            carregando={acaoEmAndamento === 'rascunho'}
            disabled={enviando && acaoEmAndamento !== 'rascunho'}
          >
            Salvar rascunho
          </Botao>
          <Botao
            type="button"
            onClick={publicar}
            carregando={acaoEmAndamento === 'publicar'}
            disabled={lotes.length === 0 || (enviando && acaoEmAndamento !== 'publicar')}
          >
            Publicar
          </Botao>
        </div>
      </footer>
    </form>
  )
}

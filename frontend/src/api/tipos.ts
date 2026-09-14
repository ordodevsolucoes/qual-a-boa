export type SituacaoEvento = 'RASCUNHO' | 'PUBLICADO'

export interface EventoResponse {
  id: string
  titulo: string
  descricao: string
  areaConhecimento: string
  inicio: string
  termino: string
  logradouro: string
  numero: string
  bairro: string
  cidade: string
  uf: string
  cep: string
  capacidadeTotal: number
  situacao: SituacaoEvento
  criadoEm: string
  atualizadoEm: string
}

export interface EventoRequest {
  titulo: string
  descricao: string
  areaConhecimento: string
  inicio: string
  termino: string
  logradouro: string
  numero: string
  bairro: string
  cidade: string
  uf: string
  cep: string
  capacidadeTotal: number
}

export interface LoteIngressoResponse {
  id: string
  nome: string
  quantidade: number
  preco: number
  vigenciaInicio: string
  vigenciaFim: string
}

export interface LoteIngressoRequest {
  nome: string
  quantidade: number
  preco: number
  vigenciaInicio: string
  vigenciaFim: string
}

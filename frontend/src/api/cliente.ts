const URL_BASE = import.meta.env.VITE_API_URL

const CHAVE_TOKEN = 'qualaboa:token'

export function obterToken(): string | null {
  return localStorage.getItem(CHAVE_TOKEN)
}

export function definirToken(token: string): void {
  localStorage.setItem(CHAVE_TOKEN, token)
}

export function limparToken(): void {
  localStorage.removeItem(CHAVE_TOKEN)
}

interface ErroDeCampo {
  campo: string
  mensagem: string
}

interface RespostaProblema {
  status: number
  detail: string
  regra?: string
  errors?: ErroDeCampo[]
}

// carrega os campos do ProblemDetail (RFC 9457) devolvido pela API para a tela
// distribuir "detail" e "errors" sem precisar conhecer o formato do backend
export class ErroDaApi extends Error {
  status: number
  detail: string
  regra?: string
  errors: ErroDeCampo[]

  constructor(status: number, detail: string, regra?: string, errors: ErroDeCampo[] = []) {
    super(detail)
    this.status = status
    this.detail = detail
    this.regra = regra
    this.errors = errors
  }
}

export async function requisicao(caminho: string, opcoes: RequestInit = {}): Promise<Response> {
  const token = obterToken()
  const cabecalhos = new Headers(opcoes.headers)
  if (opcoes.body && !cabecalhos.has('Content-Type')) {
    cabecalhos.set('Content-Type', 'application/json')
  }
  if (token) {
    cabecalhos.set('Authorization', `Bearer ${token}`)
  }

  const resposta = await fetch(`${URL_BASE}${caminho}`, { ...opcoes, headers: cabecalhos })

  // sessao expirada ou token invalido: nao ha como a tela atual continuar autenticada
  if (resposta.status === 401) {
    limparToken()
    window.location.href = '/login'
  }

  if (!resposta.ok) {
    const tipoDeConteudo = resposta.headers.get('content-type') ?? ''
    if (tipoDeConteudo.includes('application/problem+json')) {
      const problema = (await resposta.json()) as RespostaProblema
      throw new ErroDaApi(problema.status, problema.detail, problema.regra, problema.errors ?? [])
    }
    throw new ErroDaApi(resposta.status, 'Erro inesperado ao comunicar com a API')
  }

  return resposta
}

export async function verificarSaudeDaApi(): Promise<boolean> {
  try {
    const resposta = await fetch(`${URL_BASE}/actuator/health`)
    return resposta.ok
  } catch {
    return false
  }
}

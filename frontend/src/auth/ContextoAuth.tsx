import { createContext, useCallback, useContext, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { definirToken, limparToken, requisicao } from '../api/cliente'

export type PapelUsuario = 'PARTICIPANTE' | 'LOCAL_DE_CURSO'

interface Usuario {
  papel: PapelUsuario
}

interface ContextoAuthValor {
  autenticado: boolean
  usuario: Usuario | null
  entrar: (email: string, senha: string) => Promise<void>
  sair: () => void
}

const ContextoAuth = createContext<ContextoAuthValor | null>(null)

interface RespostaLogin {
  token: string
}

// o backend so devolve o token; o papel vem embutido no claim "papel" do JWT, e so
// serve para a interface decidir o que mostrar - quem valida a assinatura e o backend
function papelDoToken(token: string): PapelUsuario {
  const payloadCodificado = token.split('.')[1]
  const base64 = payloadCodificado.replace(/-/g, '+').replace(/_/g, '/')
  const preenchido = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')
  const payload = JSON.parse(atob(preenchido)) as { papel: PapelUsuario }
  return payload.papel
}

export function ProvedorAuth({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null)

  const entrar = useCallback(async (email: string, senha: string) => {
    const resposta = await requisicao('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, senha }),
    })
    const dados = (await resposta.json()) as RespostaLogin
    definirToken(dados.token)
    setUsuario({ papel: papelDoToken(dados.token) })
  }, [])

  const sair = useCallback(() => {
    limparToken()
    setUsuario(null)
  }, [])

  const valor = useMemo<ContextoAuthValor>(
    () => ({ autenticado: usuario !== null, usuario, entrar, sair }),
    [usuario, entrar, sair],
  )

  return <ContextoAuth.Provider value={valor}>{children}</ContextoAuth.Provider>
}

export function useAuth(): ContextoAuthValor {
  const contexto = useContext(ContextoAuth)
  if (!contexto) {
    throw new Error('useAuth precisa ser usado dentro de ProvedorAuth')
  }
  return contexto
}

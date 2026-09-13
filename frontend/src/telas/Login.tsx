import { useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { Botao } from '../componentes/Botao'
import { Campo } from '../componentes/Campo'
import { useAuth } from '../auth/ContextoAuth'
import { ErroDaApi } from '../api/cliente'
import './Login.css'

export function Login() {
  const { entrar } = useAuth()
  const navigate = useNavigate()

  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [carregando, setCarregando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  async function aoSubmeter(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    setErro(null)
    setCarregando(true)
    try {
      await entrar(email, senha)
      navigate('/', { replace: true })
    } catch (excecao) {
      // so a mensagem que a API devolveu vai para a tela, sem inventar texto proprio
      setErro(excecao instanceof ErroDaApi ? excecao.detail : 'Erro inesperado ao comunicar com a API')
    } finally {
      setCarregando(false)
    }
  }

  return (
    <div className="login">
      <form className="login__cartao" onSubmit={aoSubmeter}>
        <h1 className="login__titulo">
          QUAL A <span className="login__titulo-acento">BOA</span>
        </h1>
        <p className="login__subtitulo">Acesso para organizadores</p>

        <Campo
          label="E-mail"
          type="email"
          autoComplete="email"
          required
          value={email}
          onChange={(evento) => setEmail(evento.target.value)}
        />
        <Campo
          label="Senha"
          type="password"
          autoComplete="current-password"
          required
          value={senha}
          onChange={(evento) => setSenha(evento.target.value)}
        />

        {erro && <p className="login__erro">{erro}</p>}

        <Botao type="submit" carregando={carregando} className="login__botao">
          Entrar
        </Botao>
      </form>
    </div>
  )
}

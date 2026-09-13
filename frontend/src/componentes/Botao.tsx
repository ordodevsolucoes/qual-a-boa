import type { ButtonHTMLAttributes } from 'react'
import './Botao.css'

type VarianteBotao = 'primario' | 'secundario'

interface PropsBotao extends ButtonHTMLAttributes<HTMLButtonElement> {
  variante?: VarianteBotao
  carregando?: boolean
}

export function Botao({
  variante = 'primario',
  carregando = false,
  disabled,
  children,
  className,
  ...resto
}: PropsBotao) {
  const classes = ['botao', `botao--${variante}`, className].filter(Boolean).join(' ')

  return (
    <button className={classes} disabled={disabled || carregando} {...resto}>
      {carregando ? 'Carregando...' : children}
    </button>
  )
}

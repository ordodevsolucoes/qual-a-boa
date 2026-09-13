import { useId } from 'react'
import type { InputHTMLAttributes } from 'react'
import './Campo.css'

interface PropsCampo extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  erro?: string
}

export function Campo({ label, erro, id, className, ...resto }: PropsCampo) {
  const idGerado = useId()
  const idCampo = id ?? idGerado

  return (
    <div className={['campo', className].filter(Boolean).join(' ')}>
      <label className="campo__rotulo" htmlFor={idCampo}>
        {label}
      </label>
      <input
        id={idCampo}
        className="campo__entrada"
        aria-invalid={erro ? true : undefined}
        {...resto}
      />
      {erro && <div className="campo__erro">{erro}</div>}
    </div>
  )
}

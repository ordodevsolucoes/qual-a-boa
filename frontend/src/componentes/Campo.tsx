import { useId } from 'react'
import type { InputHTMLAttributes, ReactNode, SelectHTMLAttributes, TextareaHTMLAttributes } from 'react'
import './Campo.css'

interface OpcaoCampo {
  valor: string
  rotulo: string
}

interface PropsCampoComuns {
  label: string
  erro?: string
  className?: string
}

interface PropsCampoInput extends PropsCampoComuns, InputHTMLAttributes<HTMLInputElement> {
  como?: 'input'
}

interface PropsCampoTextarea extends PropsCampoComuns, TextareaHTMLAttributes<HTMLTextAreaElement> {
  como: 'textarea'
}

interface PropsCampoSelect extends PropsCampoComuns, SelectHTMLAttributes<HTMLSelectElement> {
  como: 'select'
  opcoes: OpcaoCampo[]
}

type PropsCampo = PropsCampoInput | PropsCampoTextarea | PropsCampoSelect

export function Campo(props: PropsCampo) {
  const idGerado = useId()
  const idCampo = props.id ?? idGerado
  const classeContainer = ['campo', props.className].filter(Boolean).join(' ')

  let entrada: ReactNode

  if (props.como === 'textarea') {
    const { label: _label, erro, className: _className, como: _como, ...resto } = props
    entrada = (
      <textarea
        id={idCampo}
        className="campo__entrada campo__entrada--textarea"
        aria-invalid={erro ? true : undefined}
        {...resto}
      />
    )
  } else if (props.como === 'select') {
    const { label: _label, erro, className: _className, como: _como, opcoes, ...resto } = props
    entrada = (
      <select id={idCampo} className="campo__entrada" aria-invalid={erro ? true : undefined} {...resto}>
        <option value="" disabled>
          Selecione
        </option>
        {opcoes.map((opcao) => (
          <option key={opcao.valor} value={opcao.valor}>
            {opcao.rotulo}
          </option>
        ))}
      </select>
    )
  } else {
    const { label: _label, erro, className: _className, como: _como, ...resto } = props
    entrada = (
      <input id={idCampo} className="campo__entrada" aria-invalid={erro ? true : undefined} {...resto} />
    )
  }

  return (
    <div className={classeContainer}>
      <label className="campo__rotulo" htmlFor={idCampo}>
        {props.label}
      </label>
      {entrada}
      {props.erro && <div className="campo__erro">{props.erro}</div>}
    </div>
  )
}

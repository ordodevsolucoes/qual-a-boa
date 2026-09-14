import './Etiqueta.css'

export type SituacaoEvento = 'RASCUNHO' | 'PUBLICADO'

interface PropsEtiqueta {
  situacao: SituacaoEvento
}

export function Etiqueta({ situacao }: PropsEtiqueta) {
  return (
    <span className={`etiqueta etiqueta--${situacao.toLowerCase()}`}>
      {situacao}
    </span>
  )
}

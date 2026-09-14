import { afterEach, describe, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen, within } from '@testing-library/react'
import { FormularioEvento } from '../telas/FormularioEvento'

afterEach(() => {
  cleanup()
  vi.unstubAllGlobals()
})

function paraDatetimeLocal(data: Date): string {
  const preencher = (numero: number) => String(numero).padStart(2, '0')
  return `${data.getFullYear()}-${preencher(data.getMonth() + 1)}-${preencher(data.getDate())}T${preencher(data.getHours())}:${preencher(data.getMinutes())}`
}

function preencherCamposValidos(inicio: Date, termino: Date) {
  fireEvent.change(screen.getByLabelText('Titulo'), { target: { value: 'Semana da Computacao' } })
  fireEvent.change(screen.getByLabelText('Descricao'), { target: { value: 'Uma descricao valida para o evento' } })
  fireEvent.change(screen.getByLabelText('Area de conhecimento'), { target: { value: 'Tecnologia' } })
  fireEvent.change(screen.getByLabelText('Capacidade total'), { target: { value: '100' } })
  fireEvent.change(screen.getByLabelText('Inicio'), { target: { value: paraDatetimeLocal(inicio) } })
  fireEvent.change(screen.getByLabelText('Termino'), { target: { value: paraDatetimeLocal(termino) } })
  fireEvent.change(screen.getByLabelText('Logradouro'), { target: { value: 'Rua Um' } })
  fireEvent.change(screen.getByLabelText('Numero'), { target: { value: '100' } })
  fireEvent.change(screen.getByLabelText('Bairro'), { target: { value: 'Centro' } })
  fireEvent.change(screen.getByLabelText('Cidade'), { target: { value: 'Recife' } })
  fireEvent.change(screen.getByLabelText('UF'), { target: { value: 'PE' } })
  fireEvent.change(screen.getByLabelText('CEP'), { target: { value: '50000000' } })
}

describe('FormularioEvento', () => {
  it('recusa data de inicio no passado antes de enviar', async () => {
    // resposta generica: o preenchimento do CEP dispara uma busca no ViaCEP por conta
    // propria, sem relacao com o que este teste verifica
    const fetchMock = vi.fn().mockResolvedValue(new Response('{}', { status: 200 }))
    vi.stubGlobal('fetch', fetchMock)

    render(<FormularioEvento onCancelar={vi.fn()} onConcluido={vi.fn()} />)

    const inicioPassado = new Date(Date.now() - 2 * 60 * 60 * 1000)
    const terminoFuturo = new Date(Date.now() + 60 * 60 * 1000)
    preencherCamposValidos(inicioPassado, terminoFuturo)

    fireEvent.click(screen.getByRole('button', { name: 'Salvar rascunho' }))

    expect(await screen.findByText('Data de inicio nao pode ser anterior ao momento atual')).toBeInTheDocument()
    const chamouEventos = fetchMock.mock.calls.some(([url]) => String(url).includes('/api/v1/eventos'))
    expect(chamouEventos).toBe(false)
  })

  it('mostra no campo correto a mensagem de erro vinda do array errors da API', async () => {
    const corpoProblema = {
      status: 400,
      detail: 'Um ou mais campos sao invalidos',
      errors: [{ campo: 'titulo', mensagem: 'Titulo ja cadastrado para este local' }],
    }
    const respostaMock = new Response(JSON.stringify(corpoProblema), {
      status: 400,
      headers: { 'content-type': 'application/problem+json' },
    })
    const fetchMock = vi.fn().mockResolvedValue(respostaMock)
    vi.stubGlobal('fetch', fetchMock)

    render(<FormularioEvento onCancelar={vi.fn()} onConcluido={vi.fn()} />)

    const inicioFuturo = new Date(Date.now() + 60 * 60 * 1000)
    const terminoFuturo = new Date(Date.now() + 2 * 60 * 60 * 1000)
    preencherCamposValidos(inicioFuturo, terminoFuturo)

    fireEvent.click(screen.getByRole('button', { name: 'Salvar rascunho' }))

    const containerTitulo = screen.getByLabelText('Titulo').closest('.campo') as HTMLElement
    expect(await within(containerTitulo).findByText('Titulo ja cadastrado para este local')).toBeInTheDocument()
  })
})

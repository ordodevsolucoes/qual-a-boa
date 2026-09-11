import { useEffect, useState } from 'react'
import { verificarSaudeDaApi } from './api/cliente'

type EstadoConexao = 'verificando' | 'online' | 'offline'

function App() {
  const [estado, setEstado] = useState<EstadoConexao>('verificando')

  useEffect(() => {
    verificarSaudeDaApi().then((ok) => setEstado(ok ? 'online' : 'offline'))
  }, [])

  return (
    <p>
      API: {estado}
    </p>
  )
}

export default App

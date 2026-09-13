import { useEffect, useState } from 'react'
import { Route, Routes } from 'react-router-dom'
import { verificarSaudeDaApi } from './api/cliente'
import { RotaProtegida } from './auth/RotaProtegida'
import { Login } from './telas/Login'

type EstadoConexao = 'verificando' | 'online' | 'offline'

// pagina inicial provisoria: so confirma que a API esta de pe ate a tela do organizador existir
function PaginaInicial() {
  const [estado, setEstado] = useState<EstadoConexao>('verificando')

  useEffect(() => {
    verificarSaudeDaApi().then((ok) => setEstado(ok ? 'online' : 'offline'))
  }, [])

  return <p>API: {estado}</p>
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/"
        element={
          <RotaProtegida>
            <PaginaInicial />
          </RotaProtegida>
        }
      />
    </Routes>
  )
}

export default App

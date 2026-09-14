import { Route, Routes } from 'react-router-dom'
import { RotaProtegida } from './auth/RotaProtegida'
import { Login } from './telas/Login'
import { Painel } from './telas/Painel'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/"
        element={
          <RotaProtegida>
            <Painel />
          </RotaProtegida>
        }
      />
    </Routes>
  )
}

export default App

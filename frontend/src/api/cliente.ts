const URL_BASE = import.meta.env.VITE_API_URL

export async function verificarSaudeDaApi(): Promise<boolean> {
  try {
    const resposta = await fetch(`${URL_BASE}/actuator/health`)
    return resposta.ok
  } catch {
    return false
  }
}

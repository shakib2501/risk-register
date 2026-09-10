export type Risk = {
  id: number
  title: string
  category: string
  owner: string
  status: string
  inherentScore: number
  residualScore: number
  residualSeverity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  mitigationCount: number
}

export type CreateRiskInput = {
  title: string
  description: string
  category: string
  owner: string
  likelihood: number
  impact: number
}

export async function getRisks(): Promise<Risk[]> {
  const response = await fetch('/api/risks')
  if (!response.ok) throw new Error('Unable to load risks')
  return response.json() as Promise<Risk[]>
}

export async function createRisk(risk: CreateRiskInput): Promise<Risk> {
  const response = await fetch('/api/risks', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(risk),
  })
  if (!response.ok) throw new Error('Unable to create risk')
  return response.json() as Promise<Risk>
}

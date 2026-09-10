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

export async function getRisks(): Promise<Risk[]> {
  const response = await fetch('/api/risks')
  if (!response.ok) throw new Error('Unable to load risks')
  return response.json() as Promise<Risk[]>
}

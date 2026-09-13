export type RiskCategory = 'OPERATIONAL' | 'FINANCIAL' | 'COMPLIANCE' | 'SECURITY' | 'STRATEGIC'
export type RiskStatus = 'OPEN' | 'MITIGATING' | 'CLOSED'
export type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export type Risk = {
  id: number; title: string; description: string; category: RiskCategory; owner: string
  likelihood: number; impact: number; nextReviewDate?: string; status: RiskStatus; overdue: boolean; inherentScore: number; residualScore: number
  inherentSeverity: Severity; residualSeverity: Severity; mitigationCount: number
}

export type RiskInput = Omit<Risk, 'id' | 'overdue' | 'inherentScore' | 'residualScore' | 'inherentSeverity' | 'residualSeverity' | 'mitigationCount'>
export type Mitigation = { id: number; description: string; effectiveness: number }
export type MitigationInput = Omit<Mitigation, 'id'>
export type CreateRiskInput = RiskInput & { initialMitigation?: MitigationInput }

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await (options ? fetch(url, options) : fetch(url))
  if (!response.ok) {
    const body = await response.json().catch(() => null) as { message?: string } | null
    throw new Error(body?.message ?? 'Something went wrong. Please try again.')
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export const getRisks = () => request<Risk[]>('/api/risks')
export const createRisk = (risk: CreateRiskInput) => request<Risk>('/api/risks', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(risk) })
export const updateRisk = (id: number, risk: RiskInput) => request<Risk>(`/api/risks/${id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(risk) })
export const deleteRisk = (id: number) => request<void>(`/api/risks/${id}`, { method: 'DELETE' })
export const getMitigations = (riskId: number) => request<Mitigation[]>(`/api/risks/${riskId}/mitigations`)
export const createMitigation = (riskId: number, mitigation: MitigationInput) => request<Risk>(`/api/risks/${riskId}/mitigations`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(mitigation) })
export const updateMitigation = (riskId: number, mitigationId: number, mitigation: MitigationInput) => request<Mitigation>(`/api/risks/${riskId}/mitigations/${mitigationId}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(mitigation) })
export const deleteMitigation = (riskId: number, mitigationId: number) => request<void>(`/api/risks/${riskId}/mitigations/${mitigationId}`, { method: 'DELETE' })

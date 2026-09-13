import { fireEvent, render, screen, within } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import App from './App'

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('App', () => {
  it('shows an empty risk register dashboard', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: async () => [],
    }))

    render(<App />)

    expect(screen.getByRole('heading', { name: 'Risk Register' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Add risk' })).toBeInTheDocument()
    expect(await screen.findByText('No risks found. Add your first risk to begin tracking.')).toBeInTheDocument()
  })

  it('loads risks from the API', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: async () => [
        {
          id: 1,
          title: 'Unpatched production systems',
          category: 'SECURITY',
          owner: 'Security team',
          status: 'OPEN',
          inherentScore: 20,
          residualScore: 4,
          residualSeverity: 'LOW',
          mitigationCount: 1,
        },
      ],
    }))

    render(<App />)

    expect(await screen.findByText('Unpatched production systems')).toBeInTheDocument()
    expect(screen.getByText('Inherent: 20')).toBeInTheDocument()
    expect(screen.getByText('Residual: 4')).toBeInTheDocument()
    expect(screen.getByText('1 mitigation')).toBeInTheDocument()
    expect(fetch).toHaveBeenCalledWith('/api/risks')
  })

  it('opens the add risk form', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, json: async () => [] }))
    render(<App />)

    fireEvent.click(screen.getByRole('button', { name: 'Add risk' }))

    expect(await screen.findByRole('heading', { name: 'Add a risk' })).toBeInTheDocument()
    expect(document.querySelector('.required-legend')).not.toBeInTheDocument()
    expect(screen.getByLabelText('Title')).toBeInTheDocument()
    expect(screen.getByLabelText('Next review date')).toBeInTheDocument()
    expect(screen.getByRole('group', { name: 'Initial mitigation (optional)' })).toBeInTheDocument()
    expect(within(screen.getByRole('dialog')).getByRole('option', { name: 'Closed' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Save risk' })).toBeInTheDocument()
    expect(screen.getByText('Live inherent score: 9')).toBeInTheDocument()

    fireEvent.change(screen.getByLabelText('Likelihood'), { target: { value: '4' } })
    fireEvent.change(screen.getByLabelText('Impact'), { target: { value: '5' } })

    expect(screen.getByText('Live inherent score: 20')).toBeInTheDocument()
  })

  it('creates a risk and adds it to the dashboard', async () => {
    const createdRisk = {
      id: 2,
      title: 'Vendor outage',
      category: 'OPERATIONAL',
      owner: 'Operations team',
      status: 'OPEN',
      inherentScore: 12,
      residualScore: 12,
      residualSeverity: 'MEDIUM' as const,
      mitigationCount: 0,
    }
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: true, json: async () => [] })
      .mockResolvedValueOnce({ ok: true, json: async () => createdRisk })
    vi.stubGlobal('fetch', fetchMock)
    render(<App />)

    fireEvent.click(screen.getByRole('button', { name: 'Add risk' }))
    await screen.findByRole('heading', { name: 'Add a risk' })
    fireEvent.change(screen.getByLabelText('Title'), { target: { value: 'Vendor outage' } })
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'A critical vendor may become unavailable.' } })
    fireEvent.change(screen.getByLabelText('Owner'), { target: { value: 'Operations team' } })
    fireEvent.click(screen.getByRole('button', { name: 'Save risk' }))

    expect(await screen.findByText('Vendor outage')).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'Add a risk' })).not.toBeInTheDocument()
    expect(fetchMock).toHaveBeenLastCalledWith('/api/risks', expect.objectContaining({ method: 'POST' }))
  })

  it('shows an overdue indicator on a risk that needs review', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: async () => [{
        id: 9, title: 'Overdue risk', category: 'COMPLIANCE', owner: 'Compliance team', status: 'OPEN',
        likelihood: 3, impact: 4, inherentScore: 12, residualScore: 12,
        inherentSeverity: 'MEDIUM', residualSeverity: 'MEDIUM', mitigationCount: 0,
        nextReviewDate: '2000-01-01', overdue: true,
      }],
    }))
    render(<App />)

    expect(await screen.findByText('Overdue risk')).toBeInTheDocument()
    expect(screen.getByText('Overdue review')).toBeInTheDocument()
  })

  it('creates a closed risk with an optional initial mitigation', async () => {
    const createdRisk = {
      id: 3, title: 'Patching risk', description: 'Patching may be delayed.', category: 'SECURITY' as const,
      owner: 'Security team', likelihood: 4, impact: 5, status: 'CLOSED' as const,
      inherentScore: 20, residualScore: 4, inherentSeverity: 'CRITICAL' as const,
      residualSeverity: 'LOW' as const, mitigationCount: 1,
    }
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: true, json: async () => [] })
      .mockResolvedValueOnce({ ok: true, json: async () => createdRisk })
    vi.stubGlobal('fetch', fetchMock)
    render(<App />)

    fireEvent.click(screen.getByRole('button', { name: 'Add risk' }))
    await screen.findByRole('heading', { name: 'Add a risk' })
    fireEvent.change(screen.getByLabelText('Title'), { target: { value: 'Patching risk' } })
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'Patching may be delayed.' } })
    fireEvent.change(screen.getByLabelText('Owner'), { target: { value: 'Security team' } })
    fireEvent.change(screen.getByLabelText('Initial mitigation description'), { target: { value: 'Deploy weekly patching automation.' } })
    expect(within(screen.getByRole('dialog')).getByLabelText('Status')).toHaveValue('MITIGATING')
    expect(within(screen.getByRole('dialog')).getByRole('option', { name: 'Open' })).toBeDisabled()
    expect(within(screen.getByRole('dialog')).getByRole('option', { name: 'Closed' })).not.toBeDisabled()
    fireEvent.change(within(screen.getByRole('dialog')).getByLabelText('Status'), { target: { value: 'CLOSED' } })
    fireEvent.click(screen.getByRole('button', { name: 'Save risk' }))

    expect(await screen.findByText('Patching risk')).toBeInTheDocument()
    expect(JSON.parse(fetchMock.mock.calls[1][1].body)).toMatchObject({
      status: 'CLOSED',
      initialMitigation: { description: 'Deploy weekly patching automation.', effectiveness: 3 },
    })
  })

  it('shows a save error as a toast without hiding the dashboard', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [{
          id: 1,
          title: 'Existing risk',
          category: 'SECURITY',
          owner: 'Security team',
          status: 'OPEN',
          inherentScore: 20,
          residualScore: 20,
          residualSeverity: 'CRITICAL',
          mitigationCount: 0,
        }],
      })
      .mockResolvedValueOnce({
        ok: false,
        json: async () => ({ message: 'A risk cannot be closed without at least one mitigation' }),
      })
    vi.stubGlobal('fetch', fetchMock)
    render(<App />)

    expect(await screen.findByText('Existing risk')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Add risk' }))
    fireEvent.change(screen.getByLabelText('Title'), { target: { value: 'New closed risk' } })
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'A description.' } })
    fireEvent.change(screen.getByLabelText('Owner'), { target: { value: 'Risk owner' } })
    fireEvent.change(within(screen.getByRole('dialog')).getByLabelText('Status'), { target: { value: 'CLOSED' } })
    fireEvent.click(screen.getByRole('button', { name: 'Save risk' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('A risk cannot be closed without at least one mitigation')
    expect(screen.getByText('Existing risk')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Add a risk' })).toBeInTheDocument()
  })

  it('opens risk details to add a mitigation before closing an unmitigated risk', async () => {
    const risk = {
      id: 7,
      title: 'Unmitigated risk',
      description: 'Needs a control before it can close.',
      category: 'SECURITY',
      owner: 'Security team',
      status: 'OPEN',
      likelihood: 4,
      impact: 5,
      inherentScore: 20,
      residualScore: 20,
      inherentSeverity: 'CRITICAL',
      residualSeverity: 'CRITICAL',
      mitigationCount: 0,
    }
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: true, json: async () => [risk] })
      .mockResolvedValueOnce({ ok: true, json: async () => [] })
    vi.stubGlobal('fetch', fetchMock)
    render(<App />)

    await screen.findByText('Unmitigated risk')
    fireEvent.change(screen.getByLabelText('Actions for Unmitigated risk'), { target: { value: 'EDIT' } })

    expect(await screen.findByText('Add a mitigation before closing this risk.')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Add mitigation first' }))

    expect(await screen.findByRole('heading', { name: 'Unmitigated risk' })).toBeInTheDocument()
    expect(screen.getByText('No mitigations recorded yet.')).toBeInTheDocument()
  })

  it('opens a risk details panel from its add mitigation action', async () => {
    const risk = {
      id: 8,
      title: 'Third-party outage',
      description: 'A supplier may become unavailable.',
      category: 'OPERATIONAL' as const,
      owner: 'Operations team',
      status: 'OPEN' as const,
      likelihood: 3, impact: 4, inherentScore: 12, residualScore: 12,
      inherentSeverity: 'MEDIUM' as const, residualSeverity: 'MEDIUM' as const, mitigationCount: 0,
    }
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: true, json: async () => [risk] })
      .mockResolvedValueOnce({ ok: true, json: async () => [] })
    vi.stubGlobal('fetch', fetchMock)
    render(<App />)

    expect(await screen.findByText('Third-party outage')).toBeInTheDocument()
    fireEvent.change(screen.getByLabelText('Actions for Third-party outage'), { target: { value: 'ADD_MITIGATION' } })

    expect(await screen.findByRole('heading', { name: 'Third-party outage' })).toBeInTheDocument()
    expect(screen.getByRole('dialog', { name: 'Third-party outage' })).toBeInTheDocument()
    expect(screen.getByText('Owner: Operations team')).toBeInTheDocument()
    expect(screen.getByText('Likelihood: 3')).toBeInTheDocument()
    expect(screen.getByText('Impact: 4')).toBeInTheDocument()
    expect(screen.getAllByRole('button', { name: 'Add mitigation' })).toHaveLength(1)
    expect(screen.getByLabelText('Description')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Edit risk' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Delete risk' })).toBeInTheDocument()
    expect(fetchMock).toHaveBeenLastCalledWith('/api/risks/8/mitigations')
  })

  it('adds a mitigation without showing an error toast', async () => {
    const risk = {
      id: 10, title: 'Patch management', description: 'Patches may be delayed.', category: 'SECURITY' as const,
      owner: 'Security team', status: 'OPEN' as const, likelihood: 4, impact: 5,
      inherentScore: 20, residualScore: 20, inherentSeverity: 'CRITICAL' as const,
      residualSeverity: 'CRITICAL' as const, mitigationCount: 0, overdue: false,
    }
    const mitigatedRisk = { ...risk, status: 'MITIGATING' as const, residualScore: 10, residualSeverity: 'MEDIUM' as const, mitigationCount: 1 }
    const mitigation = { id: 1, description: 'Automate patch deployment.', effectiveness: 3 }
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: true, json: async () => [risk] })
      .mockResolvedValueOnce({ ok: true, json: async () => [] })
      .mockResolvedValueOnce({ ok: true, json: async () => mitigatedRisk })
      .mockResolvedValueOnce({ ok: true, json: async () => [mitigation] })
    vi.stubGlobal('fetch', fetchMock)
    render(<App />)

    await screen.findByText('Patch management')
    fireEvent.click(screen.getByRole('button', { name: /Patch management/ }))
    await screen.findByRole('dialog', { name: 'Patch management' })
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: mitigation.description } })
    fireEvent.submit(screen.getByLabelText('Description').closest('form')!)

    expect(await screen.findByText(mitigation.description)).toBeInTheDocument()
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
    expect(fetchMock).toHaveBeenCalledWith('/api/risks/10/mitigations', expect.objectContaining({ method: 'POST' }))
  })
})

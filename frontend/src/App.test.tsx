import { fireEvent, render, screen } from '@testing-library/react'
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
    expect(screen.getByText('Residual: 4')).toBeInTheDocument()
    expect(fetch).toHaveBeenCalledWith('/api/risks')
  })

  it('opens the add risk form', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, json: async () => [] }))
    render(<App />)

    fireEvent.click(screen.getByRole('button', { name: 'Add risk' }))

    expect(await screen.findByRole('heading', { name: 'Add a risk' })).toBeInTheDocument()
    expect(screen.getByLabelText('Title')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Save risk' })).toBeInTheDocument()
  })
})

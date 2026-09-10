import { render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import App from './App'

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('App', () => {
  it('shows an empty risk register dashboard', () => {
    render(<App />)

    expect(screen.getByRole('heading', { name: 'Risk Register' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Add risk' })).toBeInTheDocument()
    expect(screen.getByText('No risks found. Add your first risk to begin tracking.')).toBeInTheDocument()
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
})

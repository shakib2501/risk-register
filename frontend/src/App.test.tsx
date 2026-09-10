import { render, screen } from '@testing-library/react'
import App from './App'

describe('App', () => {
  it('shows an empty risk register dashboard', () => {
    render(<App />)

    expect(screen.getByRole('heading', { name: 'Risk Register' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Add risk' })).toBeInTheDocument()
    expect(screen.getByText('No risks found. Add your first risk to begin tracking.')).toBeInTheDocument()
  })
})

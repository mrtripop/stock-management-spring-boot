import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../lib/api'
import { Button } from '../atoms/Button'
import { Input } from '../atoms/Input'
import { FormField } from '../molecules/FormField'
import { Spinner } from '../atoms/Spinner'

export default function Login({ onLogin, initialTab = 'login' }) {
  const navigate = useNavigate()
  const [tab, setTab] = useState(initialTab)
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [totpCode, setTotpCode] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [showMfa, setShowMfa] = useState(false)
  const [tempToken, setTempToken] = useState(null)

  const handleLogin = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await api.request('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
      })
      const payload = res.data
      if (payload?.mfaRequired || typeof payload?.tempToken === 'string') {
        setTempToken(payload.tempToken)
        setShowMfa(true)
      } else {
        setError('Unexpected response format')
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleMfa = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      api.setToken(tempToken)
      const res = await api.request('/auth/verify-totp', {
        method: 'POST',
        body: JSON.stringify({ code: totpCode }),
      })
      const payload = res.data
      if (payload?.token) {
        api.setToken(payload.token)
        onLogin()
        navigate('/')
      } else {
        onLogin()
        navigate('/')
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleRegister = async (e) => {
    e.preventDefault()
    setError('')
    if (password !== confirmPassword) {
      setError('Passwords do not match')
      return
    }
    setLoading(true)
    try {
      await api.request('/auth/register', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
      })
      setTab('login')
      setError('Registration successful. Please log in.')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-[#134e4a] to-[#0d9488]">
      <div className="w-full max-w-[400px] bg-white rounded-[var(--radius-lg)] shadow-[var(--shadow-lg)] p-8">
        {/* Logo */}
        <div className="text-center mb-6">
          <div className="w-12 h-12 bg-[var(--color-primary)] rounded-xl flex items-center justify-center text-white text-xl font-bold mx-auto">
            P
          </div>
          <h1 className="text-base font-bold text-[var(--color-text-primary)] mt-3">Pharmacy Stock Manager</h1>
        </div>

        {/* MFA Step */}
        {showMfa ? (
          <form onSubmit={handleMfa}>
            <p className="text-sm text-[var(--color-text-secondary)] mb-4 text-center">Enter your 6-digit verification code</p>
            {error && <div className="bg-red-50 text-red-800 text-xs p-2.5 rounded-[var(--radius-md)] mb-3">{error}</div>}
            <FormField label="Verification Code" required>
              <Input
                value={totpCode}
                onChange={(e) => setTotpCode(e.target.value)}
                placeholder="000000"
                maxLength={6}
                className="text-center text-lg tracking-widest"
              />
            </FormField>
            <Button type="submit" className="w-full justify-center" loading={loading}>Verify</Button>
          </form>
        ) : (
          <>
            {/* Tab toggle */}
            <div className="flex border-b border-[var(--color-border-light)] mb-5">
              <button
                onClick={() => { setTab('login'); setError('') }}
                className={`flex-1 py-2 text-sm font-medium transition-colors ${tab === 'login' ? 'text-[var(--color-primary)] border-b-2 border-[var(--color-primary)]' : 'text-[var(--color-text-muted)]'}`}
              >
                Login
              </button>
              <button
                onClick={() => { setTab('register'); setError('') }}
                className={`flex-1 py-2 text-sm font-medium transition-colors ${tab === 'register' ? 'text-[var(--color-primary)] border-b-2 border-[var(--color-primary)]' : 'text-[var(--color-text-muted)]'}`}
              >
                Register
              </button>
            </div>

            {error && <div className={`text-xs p-2.5 rounded-[var(--radius-md)] mb-3 ${error.includes('successful') ? 'bg-emerald-50 text-emerald-800' : 'bg-red-50 text-red-800'}`}>{error}</div>}

            {tab === 'login' ? (
              <form onSubmit={handleLogin}>
                <FormField label="Username" required>
                  <Input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="Enter username" />
                </FormField>
                <FormField label="Password" required>
                  <Input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Enter password" />
                </FormField>
                <Button type="submit" className="w-full justify-center mt-2" loading={loading}>Sign In</Button>
              </form>
            ) : (
              <form onSubmit={handleRegister}>
                <FormField label="Username" required>
                  <Input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="Choose a username" />
                </FormField>
                <FormField label="Email" required>
                  <Input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Enter email" />
                </FormField>
                <FormField label="Password" required>
                  <Input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Min 8 characters" />
                </FormField>
                <FormField label="Confirm Password" required>
                  <Input type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} placeholder="Repeat password" />
                </FormField>
                <Button type="submit" className="w-full justify-center mt-2" loading={loading}>Create Account</Button>
              </form>
            )}
          </>
        )}
      </div>
    </div>
  )
}

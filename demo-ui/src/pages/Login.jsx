import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useAuth } from '../lib/auth'
import { Button } from '../atoms/Button'
import { FormField } from '../molecules/FormField'
import { Input } from '../atoms/Input'

const loginSchema = z.object({
  username: z.string().min(1, 'Username is required'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
})

const mfaSchema = z.object({
  totpCode: z.string().length(6, 'Code must be 6 digits'),
})

export default function Login({ initialTab = 'login' }) {
  const { login, register, verifyMfa, error: authError, loading } = useAuth()
  const [tab, setTab] = useState(initialTab)
  const [mfaRequired, setMfaRequired] = useState(false)
  const [tempToken, setTempToken] = useState(null)

  const loginForm = useForm({ resolver: zodResolver(loginSchema), defaultValues: { username: localStorage.getItem('remembered_username') || '', password: '' } })
  const registerForm = useForm({ resolver: zodResolver(loginSchema), defaultValues: { username: '', password: '' } })
  const mfaForm = useForm({ resolver: zodResolver(mfaSchema), defaultValues: { totpCode: '' } })

  const handleLogin = async (data) => {
    try {
      const result = await login(data.username, data.password)
      if (result?.mfaRequired) {
        setMfaRequired(true)
        setTempToken(result.tempToken)
      } else if (result?.success) {
        localStorage.setItem('remembered_username', data.username)
      }
    } catch {}
  }

  const handleRegister = async (data) => {
    try {
      await register(data.username, data.password)
      setTab('login')
    } catch {}
  }

  const handleMfa = async (data) => {
    try {
      await verifyMfa(tempToken, data.totpCode)
    } catch {}
  }

  return (
    <div className="min-h-screen bg-[var(--color-background)] flex items-center justify-center p-4">
      <div className="w-full max-w-sm">
        <div className="bg-[var(--color-surface)] rounded-[var(--radius-xl)] shadow-[var(--shadow-lg)] p-8">
          <div className="flex justify-center mb-6">
            <div className="w-12 h-12 bg-[var(--color-primary)] rounded-[var(--radius-lg)] flex items-center justify-center text-white text-xl font-bold">P</div>
          </div>
          <h1 className="text-xl font-semibold text-[var(--color-text-primary)] text-center mb-1">PharmStock</h1>
          <p className="text-sm text-[var(--color-text-muted)] text-center mb-6">{tab === 'login' ? 'Sign in to your account' : 'Create a new account'}</p>

          <div className="flex mb-6 bg-[var(--color-background)] rounded-[var(--radius-md)] p-1">
            <button type="button" onClick={() => setTab('login')} className={`flex-1 py-2 text-sm font-medium rounded-[var(--radius-md)] transition-colors ${tab === 'login' ? 'bg-[var(--color-surface)] shadow-[var(--shadow-sm)] text-[var(--color-text-primary)]' : 'text-[var(--color-text-muted)]'}`}>Sign In</button>
            <button type="button" onClick={() => setTab('register')} className={`flex-1 py-2 text-sm font-medium rounded-[var(--radius-md)] transition-colors ${tab === 'register' ? 'bg-[var(--color-surface)] shadow-[var(--shadow-sm)] text-[var(--color-text-primary)]' : 'text-[var(--color-text-muted)]'}`}>Register</button>
          </div>

          {authError && <div className="bg-[var(--color-danger-subtle)] text-[var(--color-danger-text)] text-sm rounded-[var(--radius-md)] px-3 py-2 mb-4">{authError}</div>}

          {tab === 'login' && !mfaRequired && (
            <form onSubmit={loginForm.handleSubmit(handleLogin)} className="space-y-4">
              <FormField label="Username" error={loginForm.formState.errors.username?.message} required>
                <Input {...loginForm.register('username')} placeholder="Enter username" />
              </FormField>
              <FormField label="Password" error={loginForm.formState.errors.password?.message} required>
                <Input {...loginForm.register('password')} type="password" placeholder="Enter password" />
              </FormField>
              <label className="flex items-center gap-2 text-sm text-[var(--color-text-secondary)]">
                <input type="checkbox" defaultChecked={!!localStorage.getItem('remembered_username')} onChange={(e) => { if (!e.target.checked) localStorage.removeItem('remembered_username') }} className="rounded" />
                Remember username
              </label>
              <Button type="submit" loading={loading} fullWidth>Sign in</Button>
            </form>
          )}

          {tab === 'register' && (
            <form onSubmit={registerForm.handleSubmit(handleRegister)} className="space-y-4">
              <FormField label="Username" error={registerForm.formState.errors.username?.message} required>
                <Input {...registerForm.register('username')} placeholder="Choose a username" />
              </FormField>
              <FormField label="Password" error={registerForm.formState.errors.password?.message} required>
                <Input {...registerForm.register('password')} type="password" placeholder="At least 8 characters" />
              </FormField>
              <Button type="submit" loading={loading} fullWidth>Register</Button>
            </form>
          )}

          {mfaRequired && (
            <form onSubmit={mfaForm.handleSubmit(handleMfa)} className="space-y-4">
              <p className="text-sm text-[var(--color-text-secondary)] text-center">Enter the 6-digit code from your authenticator app</p>
              <FormField label="TOTP Code" error={mfaForm.formState.errors.totpCode?.message} required>
                <Input {...mfaForm.register('totpCode')} placeholder="000000" maxLength={6} autoComplete="one-time-code" />
              </FormField>
              <Button type="submit" loading={loading} fullWidth>Verify</Button>
            </form>
          )}
        </div>
      </div>
    </div>
  )
}

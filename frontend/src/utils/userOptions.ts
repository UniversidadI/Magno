/**
 * Helpers for building searchable options for user-related selects
 * (functionaries, students and external allies).
 */

export interface UserLike {
  id?: number | string
  full_name?: string
  fullName?: string
  identification_number?: string
  identificationNumber?: string
  identification?: string
  user_code?: string
  userCode?: string
  email?: string
  name?: string
}

export interface UserOption {
  label: string
  value: any
  full_name: string
  identification_number: string
  identification: string
  user_code: string
  email: string
  search: string
}

const first = (...values: any[]) =>
  values.find(v => v !== undefined && v !== null && String(v).trim() !== '')

export function buildUserOption(user: UserLike): UserOption {
  const fullName = String(first(user.full_name, user.fullName, user.name) ?? '')
  const identification = String(
    first(user.identification_number, user.identificationNumber, user.identification) ?? ''
  )
  const userCode = String(first(user.user_code, user.userCode) ?? '')
  const email = String(first(user.email) ?? '')

  const label = [fullName, identification, userCode].filter(Boolean).join(' - ')

  return {
    label: label || String(user.id ?? ''),
    value: user.id,
    full_name: fullName,
    identification_number: identification,
    identification,
    user_code: userCode,
    email,
    search: [fullName, identification, userCode, email].filter(Boolean).join(' '),
  }
}

export function buildUserOptions(users: UserLike[] = []): UserOption[] {
  return users.map(buildUserOption)
}

export function userAutocompleteFilter(
  value: any,
  query: string,
  item?: any
): boolean {
  const needle = String(query ?? '').trim().toLowerCase()
  if (!needle) return true

  const raw = item?.raw ?? item ?? {}
  const candidates = [
    value,
    raw.label,
    raw.search,
    raw.full_name,
    raw.fullName,
    raw.identification_number,
    raw.identificationNumber,
    raw.identification,
    raw.user_code,
    raw.userCode,
    raw.code,
    raw.email,
    raw.name,
  ]

  return candidates.some(
    candidate => candidate != null && String(candidate).toLowerCase().includes(needle)
  )
}

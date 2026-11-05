import { describe, it, expect } from 'vitest'
import {
  buildUserOption,
  buildUserOptions,
  userAutocompleteFilter,
} from '../userOptions'

const user = {
  id: 7,
  full_name: 'EDER MARTINEZ',
  identification_number: '123456789',
  user_code: 'EST2020123',
  email: 'eder@unibague.edu.co',
}

describe('buildUserOption', () => {
  it('builds an informative label with identification and code', () => {
    const option = buildUserOption(user)

    expect(option).toMatchObject({
      label: 'EDER MARTINEZ - 123456789 - EST2020123',
      value: 7,
      full_name: 'EDER MARTINEZ',
      identification_number: '123456789',
      user_code: 'EST2020123',
      email: 'eder@unibague.edu.co',
    })
  })

  it('omits missing parts from the label', () => {
    const option = buildUserOption({ id: 1, full_name: 'JANE DOE' })

    expect(option.label).toBe('JANE DOE')
    expect(option.identification_number).toBe('')
    expect(option.user_code).toBe('')
  })

  it('supports camelCase and legacy fields as fallbacks', () => {
    const option = buildUserOption({
      id: 2,
      fullName: 'JOHN ROE',
      identificationNumber: '987',
      userCode: 'DOC1',
    })

    expect(option.label).toBe('JOHN ROE - 987 - DOC1')
  })

  it('builds a list of options', () => {
    expect(buildUserOptions([user])).toHaveLength(1)
    expect(buildUserOptions()).toEqual([])
  })
})

describe('userAutocompleteFilter', () => {
  const item = { raw: buildUserOption(user) }

  it('matches by surname only', () => {
    expect(userAutocompleteFilter('EDER MARTINEZ - 123456789 - EST2020123', 'martinez', item)).toBe(true)
  })

  it('matches by identification number', () => {
    expect(userAutocompleteFilter('EDER MARTINEZ', '123456789', item)).toBe(true)
  })

  it('matches by user code', () => {
    expect(userAutocompleteFilter('EDER MARTINEZ', 'EST2020', item)).toBe(true)
  })

  it('matches by email', () => {
    expect(userAutocompleteFilter('EDER MARTINEZ', 'unibague', item)).toBe(true)
  })

  it('is case-insensitive', () => {
    expect(userAutocompleteFilter('EDER MARTINEZ', 'eder', item)).toBe(true)
  })

  it('returns true when the query is empty', () => {
    expect(userAutocompleteFilter('EDER MARTINEZ', '', item)).toBe(true)
    expect(userAutocompleteFilter('EDER MARTINEZ', '   ', item)).toBe(true)
  })

  it('returns false when there is no match', () => {
    expect(userAutocompleteFilter('EDER MARTINEZ', 'zzz', item)).toBe(false)
  })

  it('falls back to the value when raw is missing', () => {
    expect(userAutocompleteFilter('PLAIN OPTION', 'plain', undefined)).toBe(true)
    expect(userAutocompleteFilter('PLAIN OPTION', 'missing', undefined)).toBe(false)
  })
})

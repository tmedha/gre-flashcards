// Typed client for the Verbatim backend API.

export interface Card {
  id: number
  word: string
  definition: string
  partOfSpeech: string
  difficulty: number
  exampleSentences: string[]
  boxNumber: number
}

export interface QuizQuestion {
  wordId: number
  word: string
  partOfSpeech: string
  difficulty: number
  options: string[]
}

export interface QuizResult {
  correct: boolean
  correctDefinition: string
  progress: unknown
}

export interface DifficultyBreakdown {
  difficulty: number
  totalWords: number
  seenWords: number
  masteredWords: number
  accuracyPercent: number
}

export interface Analytics {
  totalWords: number
  seenWords: number
  masteredWords: number
  masteryPercent: number
  accuracyPercent: number
  quizAttempts: number
  streak: number
  wordsToday: number
  byDifficulty: DifficultyBreakdown[]
}

async function json<T>(res: Response): Promise<T> {
  if (!res.ok) throw new Error(`Request failed: ${res.status}`)
  return res.json() as Promise<T>
}

export const api = {
  reviewSession: () => fetch('/api/review/session').then(json<Card[]>),

  grade: (wordId: number, correct: boolean) =>
    fetch(`/api/review/${wordId}/grade`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ correct }),
    }).then((r) => {
      if (!r.ok) throw new Error(`Grade failed: ${r.status}`)
    }),

  quizNext: async (): Promise<QuizQuestion | null> => {
    const res = await fetch('/api/quiz/next')
    if (res.status === 204) return null
    return json<QuizQuestion>(res)
  },

  quizAnswer: (wordId: number, selectedDefinition: string) =>
    fetch(`/api/quiz/${wordId}/answer`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ selectedDefinition }),
    }).then(json<QuizResult>),

  analytics: () => fetch('/api/analytics').then(json<Analytics>),

  reset: () =>
    fetch('/api/progress/reset', { method: 'POST' }).then((r) => {
      if (!r.ok) throw new Error(`Reset failed: ${r.status}`)
    }),
}

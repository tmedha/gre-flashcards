import { useCallback, useEffect, useState } from 'react'
import { api, type Analytics, type Card, type QuizQuestion } from './api'
import { Flashcard } from './components/Flashcard'
import { QuizCard } from './components/QuizCard'
import { AnalyticsView } from './components/AnalyticsView'

const MODES = [
  { key: 'flashcard', label: 'Flashcard Review' },
  { key: 'quiz', label: 'MCQ Quiz' },
  { key: 'analytics', label: 'Analytics' },
] as const

type ModeKey = (typeof MODES)[number]['key']

export default function App() {
  const [modeIndex, setModeIndex] = useState(0)
  const [analytics, setAnalytics] = useState<Analytics | null>(null)
  const [queue, setQueue] = useState<Card[]>([])
  const [question, setQuestion] = useState<QuizQuestion | null>(null)

  const mode: ModeKey = MODES[modeIndex].key

  const refreshAnalytics = useCallback(async () => {
    setAnalytics(await api.analytics())
  }, [])

  const loadSession = useCallback(async () => {
    setQueue(await api.reviewSession())
  }, [])

  const loadQuestion = useCallback(async () => {
    setQuestion(await api.quizNext())
  }, [])

  useEffect(() => {
    refreshAnalytics()
    loadSession()
  }, [refreshAnalytics, loadSession])

  useEffect(() => {
    if (mode === 'quiz' && !question) loadQuestion()
  }, [mode, question, loadQuestion])

  async function gradeCard(correct: boolean) {
    const [current, ...rest] = queue
    await api.grade(current.id, correct)
    // "Review Again" keeps the word in the session; "Mastered" removes it.
    const next = correct ? rest : [...rest, current]
    if (next.length === 0) {
      await loadSession()
    } else {
      setQueue(next)
    }
    refreshAnalytics()
  }

  async function answerQuiz(selectedDefinition: string) {
    const result = await api.quizAnswer(question!.wordId, selectedDefinition)
    refreshAnalytics()
    return result
  }

  async function reset() {
    await api.reset()
    setQuestion(null)
    await Promise.all([refreshAnalytics(), loadSession()])
    if (mode === 'quiz') loadQuestion()
  }

  const changeMode = (delta: number) => {
    setModeIndex((i) => (i + delta + MODES.length) % MODES.length)
  }

  return (
    <div className="app">
      <header className="header">
        <h1>
          Verba<span className="brand-accent">tim</span>
        </h1>
        <button className="reset-btn" onClick={reset} title="Reset all progress">
          ↺
        </button>
      </header>

      {mode === 'flashcard' &&
        (queue.length > 0 ? (
          <Flashcard key={queue[0].id} card={queue[0]} onGrade={gradeCard} />
        ) : (
          <div className="card">
            <p className="hint-label">🎉 All caught up! No words due right now.</p>
          </div>
        ))}

      {mode === 'quiz' &&
        (question ? (
          <QuizCard
            key={question.wordId}
            question={question}
            onAnswer={answerQuiz}
            onNext={loadQuestion}
          />
        ) : (
          <div className="card">
            <p className="hint-label">No words available to quiz.</p>
          </div>
        ))}

      {mode === 'analytics' &&
        (analytics ? (
          <AnalyticsView analytics={analytics} />
        ) : (
          <div className="empty">Loading…</div>
        ))}

      <hr className="divider" />

      <div className="stats">
        <div title="Unique words you've reviewed today, across flashcards and the quiz.">
          <div className="label">Studied Today</div>
          <div className="value">{analytics?.wordsToday ?? 0}</div>
        </div>
        <div title="Percentage of multiple-choice questions you've answered correctly. Flashcard ratings do not affect this.">
          <div className="label">Quiz Accuracy</div>
          <div className="value">
            {analytics && analytics.quizAttempts > 0 ? `${analytics.accuracyPercent}%` : '—'}
          </div>
        </div>
        <div title="Consecutive days you've studied.">
          <div className="label">Streak</div>
          <div className="value">{analytics?.streak ?? 0}</div>
        </div>
      </div>

      <hr className="divider" />

      <div className="mode-row">
        <span className="mode-label">Mode</span>
        <div className="mode-switch">
          <button className="mode-nav" onClick={() => changeMode(-1)}>
            ‹
          </button>
          <div className="mode-name">{MODES[modeIndex].label}</div>
          <button className="mode-nav" onClick={() => changeMode(1)}>
            ›
          </button>
        </div>
      </div>
    </div>
  )
}

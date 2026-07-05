import { useState } from 'react'
import type { QuizQuestion } from '../api'

interface Props {
  question: QuizQuestion
  onAnswer: (selectedDefinition: string) => Promise<{ correct: boolean; correctDefinition: string }>
  onNext: () => void
}

export function QuizCard({ question, onAnswer, onNext }: Props) {
  const [selected, setSelected] = useState<string | null>(null)
  const [correctDef, setCorrectDef] = useState<string | null>(null)

  const answered = selected !== null

  async function choose(option: string) {
    if (answered) return
    setSelected(option)
    const result = await onAnswer(option)
    setCorrectDef(result.correctDefinition)
  }

  function optionClass(option: string): string {
    if (!answered) return 'option'
    if (option === correctDef) return 'option correct'
    if (option === selected) return 'option wrong'
    return 'option'
  }

  return (
    <div>
      <div className="quiz-prompt">
        <span className="pos-badge">{question.partOfSpeech}</span>
        <p className="word">{question.word}</p>
        <p className="sub">Choose the correct definition</p>
      </div>

      <div className="options">
        {question.options.map((option) => (
          <button
            key={option}
            className={optionClass(option)}
            disabled={answered}
            onClick={() => choose(option)}
          >
            {option}
          </button>
        ))}
      </div>

      {answered && (
        <button className="next-btn" onClick={onNext}>
          Next word →
        </button>
      )}
    </div>
  )
}

import { useState } from 'react'
import type { Card } from '../api'

interface Props {
  card: Card
  onGrade: (correct: boolean) => void
}

export function Flashcard({ card, onGrade }: Props) {
  const [revealed, setRevealed] = useState(false)
  const [showHint, setShowHint] = useState(false)

  const hint = card.exampleSentences[0]

  return (
    <div>
      <div className="card" onClick={() => setRevealed(true)}>
        {revealed ? (
          <>
            <span className="pos-badge">{card.partOfSpeech}</span>
            <p className="word">{card.word}</p>
            <p className="definition">{card.definition}</p>
            {hint && <p className="example">"{hint}"</p>}
          </>
        ) : (
          <>
            <p className="word">{card.word}</p>
            {showHint && hint ? (
              <p className="example">"{hint.replace(new RegExp(card.word, 'ig'), '_____')}"</p>
            ) : (
              <p className="hint-label">Tap to reveal</p>
            )}
            {hint && (
              <button
                className="btn-hint"
                onClick={(e) => {
                  e.stopPropagation()
                  setShowHint((v) => !v)
                }}
              >
                {showHint ? 'Hide hint' : '💡 Hint'}
              </button>
            )}
          </>
        )}
      </div>

      <div className="actions">
        <button className="btn btn-again" onClick={() => onGrade(false)}>
          Review Again
        </button>
        <button className="btn btn-mastered" onClick={() => onGrade(true)}>
          Mastered
        </button>
      </div>

      <p className="action-hint">
        Self-rate your recall to schedule the next review. <strong>Mastered</strong>{' '}
        pushes the word further out and builds Mastery; <strong>Review Again</strong>{' '}
        brings it back soon. This doesn't affect your Quiz Accuracy.
      </p>
    </div>
  )
}

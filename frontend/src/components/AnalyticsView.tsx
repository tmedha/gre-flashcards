import type { Analytics } from '../api'

interface Props {
  analytics: Analytics
}

const DIFFICULTY_LABELS = ['', 'Basic', 'Easy', 'Medium', 'Hard', 'Expert']

export function AnalyticsView({ analytics }: Props) {
  return (
    <div>
      <div className="analytics-grid">
        <div className="stat-card">
          <div className="big">{analytics.masteryPercent}%</div>
          <div className="cap">Mastery</div>
        </div>
        <div className="stat-card">
          <div className="big">
            {analytics.quizAttempts > 0 ? `${analytics.accuracyPercent}%` : '—'}
          </div>
          <div className="cap">Quiz Accuracy</div>
        </div>
        <div className="stat-card">
          <div className="big">{analytics.streak}</div>
          <div className="cap">Day streak</div>
        </div>
        <div className="stat-card">
          <div className="big">{analytics.seenWords}</div>
          <div className="cap">Words seen</div>
        </div>
        <div className="stat-card">
          <div className="big">{analytics.masteredWords}</div>
          <div className="cap">Mastered</div>
        </div>
        <div className="stat-card">
          <div className="big">{analytics.totalWords}</div>
          <div className="cap">Total words</div>
        </div>
      </div>

      <p className="legend">
        <strong>Mastery</strong> grows as you rate flashcards and reaches a word once
        it climbs to box 4 of the spaced-repetition system.{' '}
        <strong>Quiz Accuracy</strong> is scored only from the multiple-choice quiz.
      </p>

      <div className="breakdown">
        <h3>Mastery by difficulty</h3>
        {analytics.byDifficulty
          .filter((d) => d.totalWords > 0)
          .map((d) => {
            const pct = d.totalWords === 0 ? 0 : Math.round((100 * d.masteredWords) / d.totalWords)
            return (
              <div className="diff-row" key={d.difficulty}>
                <span className="diff-label">{DIFFICULTY_LABELS[d.difficulty]}</span>
                <div className="bar-track">
                  <div className="bar-fill" style={{ width: `${pct}%` }} />
                </div>
                <span className="diff-count">
                  {d.masteredWords}/{d.totalWords}
                </span>
              </div>
            )
          })}
      </div>
    </div>
  )
}

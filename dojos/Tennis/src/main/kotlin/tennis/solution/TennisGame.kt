package tennis.solution

import kotlin.math.abs

class TennisGame {

	private var pointsPlayer1: Int = 0
	private var pointsPlayer2: Int = 0

	fun determineScore() = when {
		gameIsOver()       -> determineWinningScore()
		scoreIsEven()      -> determineEvenScore()
		scoreIsAdvantage() -> determineAdvantageScore()
		else               -> determineRunningScore()
	}

	private fun determineRunningScore(): String {
		val scorePlayer1 = when (pointsPlayer1) {
			0    -> "Love"
			1    -> "Fifteen"
			2    -> "Thirty"
			else -> "Forty"
		}

		val scorePlayer2 = when (pointsPlayer2) {
			0    -> "Love"
			1    -> "Fifteen"
			2    -> "Thirty"
			else -> "Forty"
		}
		return "$scorePlayer1-$scorePlayer2"
	}

	private fun determineAdvantageScore(): String {
		return when (pointsPlayer1 > pointsPlayer2) {
			true  -> "Advantage player1"
			false -> "Advantage player2"
		}
	}

	private fun determineEvenScore(): String {
		return when (pointsPlayer1) {
			0    -> "Love-All"
			1    -> "Fifteen-All"
			2    -> "Thirty-All"
			else -> "Deuce"
		}
	}

	private fun determineWinningScore(): String {
		return when (pointsPlayer1 > pointsPlayer2) {
			true  -> "Win for player1"
			false -> "Win for player2"
		}
	}

	private fun scoreIsEven() = pointsPlayer1 == pointsPlayer2

	private fun gameIsOver() = (pointsPlayer1 >= 4 || pointsPlayer2 >= 4) && abs(pointsPlayer1 - pointsPlayer2) >= 2

	private fun scoreIsAdvantage() = abs(pointsPlayer1 - pointsPlayer2) == 1 && pointsPlayer1 >= 3 && pointsPlayer2 >= 3

	private fun P1Score() {
		pointsPlayer1++
	}

	private fun P2Score() {
		pointsPlayer2++
	}

	fun wonPoint(player: String) {
		if (player === "player1")
			P1Score()
		else
			P2Score()
	}
}

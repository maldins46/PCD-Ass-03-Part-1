package pcd.ass03.ex01.messages;

import akka.actor.ActorRef;

/**
 * Risponde al giocatore sulla sequenza scelta.
 */
public final class GuessResponseMsg implements Message {
    private final int guessedCyphers;
    private final int guessedPositions;

    public GuessResponseMsg(final int guessedCyphers, final int guessedPositions) {
        this.guessedCyphers = guessedCyphers;
        this.guessedPositions = guessedPositions;
    }

    public int getGuessedCyphers() {
        return guessedCyphers;
    }

    public int getGuessedPositions() {
        return guessedPositions;
    }
}

package pcd.ass03.ex01.messages;

import akka.actor.ActorRef;
import pcd.ass03.ex01.utils.Combination;

/**
 * Risponde al giocatore sulla sequenza scelta.
 */
public final class GuessResponseMsg implements Message {
    private final int guessedCyphers;
    private final int guessedPositions;
    private final Combination combination;

    public GuessResponseMsg(final int guessedCyphers, final int guessedPositions, final Combination combination) {
        this.guessedCyphers = guessedCyphers;
        this.guessedPositions = guessedPositions;
        this.combination = combination;
    }

    public int getGuessedCyphers() {
        return guessedCyphers;
    }

    public int getGuessedPositions() {
        return guessedPositions;
    }

    public Combination getCombination() {
        return combination;
    }
}

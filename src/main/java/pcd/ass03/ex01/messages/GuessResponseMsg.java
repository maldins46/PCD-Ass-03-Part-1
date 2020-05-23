package pcd.ass03.ex01.messages;

import akka.actor.ActorRef;

/**
 * Risponde al giocatore sulla sequenza scelta.
 */
public final class GuessResponseMsg implements Message {
    private final int guessedCyphers;
    private final int guessedPositions;

    private final ActorRef sender;

    public GuessResponseMsg(final int guessedCyphers, final int guessedPositions, ActorRef sender) {
        this.guessedCyphers = guessedCyphers;
        this.guessedPositions = guessedPositions;
        this.sender = sender;
    }

    public int getGuessedCyphers() {
        return guessedCyphers;
    }

    public int getGuessedPositions() {
        return guessedPositions;
    }

    public ActorRef getSender() { return sender;}
}

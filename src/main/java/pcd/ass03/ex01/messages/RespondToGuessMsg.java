package pcd.ass03.ex01.messages;

/**
 * Risponde al giocatore sulla sequenza scelta.
 */
public final class RespondToGuessMsg implements Message {
    private final int guessedCyphers;
    private final int guessedPositions;

    public RespondToGuessMsg(final int guessedCyphers, final int guessedPositions) {
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

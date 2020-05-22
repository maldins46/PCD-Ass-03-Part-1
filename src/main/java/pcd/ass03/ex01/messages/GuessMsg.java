package pcd.ass03.ex01.messages;

/**
 * Chiede a un giocatore quanto una sequenza scelta si avvicina a quella giusta.
 */
public final class GuessMsg implements Message {
    private final int guessedCyphers;
    private final int guessedPositions;

    public GuessMsg(final int guessedCyphers, final int guessedPositions) {
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

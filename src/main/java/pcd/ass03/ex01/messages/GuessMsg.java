package pcd.ass03.ex01.messages;

import pcd.ass03.ex01.utils.Combination;

/**
 * Chiede a un giocatore quanto una sequenza scelta si avvicina a quella giusta.
 */
public final class GuessMsg implements Message {
    private final Combination combination;

    public GuessMsg(final Combination combination) {
        this.combination = combination;
    }

    public Combination getCombination() {
        return combination;
    }
}

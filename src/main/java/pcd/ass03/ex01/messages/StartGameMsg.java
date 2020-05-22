package pcd.ass03.ex01.messages;

/**
 * Notifica all'arbitro di iniziare la partita. L'arb. crea i giocatori
 */
public final class StartGameMsg implements Message {
    private final int nPlayers;
    private final int combinationSize;

    public StartGameMsg(final int nPlayers, final int combinationSize) {
        this.nPlayers = nPlayers;
        this.combinationSize = combinationSize;
    }

    public int getnPlayers() {
        return nPlayers;
    }

    public int getCombinationSize() {
        return combinationSize;
    }
}

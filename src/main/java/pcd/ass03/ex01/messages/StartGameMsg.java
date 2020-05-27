package pcd.ass03.ex01.messages;

import akka.actor.ActorRef;

/**
 * Notifica all'arbitro di iniziare la partita. L'arb. crea i giocatori
 */
public final class StartGameMsg implements Message {
    private final ActorRef guiActor;
    private final int nPlayers;
    private final int combinationSize;

    public StartGameMsg(final ActorRef guiActor, final int nPlayers, final int combinationSize) {
        this.guiActor = guiActor;
        this.nPlayers = nPlayers;
        this.combinationSize = combinationSize;
    }

    public int getnPlayers() {
        return nPlayers;
    }

    public int getCombinationSize() {
        return combinationSize;
    }

    public ActorRef getGuiActor() {
        return guiActor;
    }
}

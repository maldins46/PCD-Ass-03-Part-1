package pcd.ass03.ex01.messages;

import akka.actor.ActorRef;

import java.util.Set;

/**
 * Notifica ai giocatori di iniziare la partita.
 */
public final class StartPlayerMsg implements Message {
    private final int combinationSize;
    private final Set<ActorRef> playersRef;
    private final ActorRef refereeRef;

    public StartPlayerMsg(final int combinationSize, final Set<ActorRef> playersRef, final ActorRef refereeRef) {
        this.combinationSize = combinationSize;
        this.playersRef = playersRef;
        this.refereeRef = refereeRef;
    }

    public int getCombinationSize() {
        return combinationSize;
    }

    public Set<ActorRef> getPlayersRef() {
        return playersRef;
    }

    public ActorRef getRefereeRef() {
        return refereeRef;
    }
}

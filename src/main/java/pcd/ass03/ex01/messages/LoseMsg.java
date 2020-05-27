package pcd.ass03.ex01.messages;

import akka.actor.ActorRef;

/*
 * Se il giocatore perde, e tutti i gioc. hanno perso, tutti i giocatori si spengono
 */
public final class LoseMsg implements Message {

    private final int nActivePlayers;
    private final ActorRef loser;

    public LoseMsg(final int nActivePlayers, final ActorRef loser){
        this.nActivePlayers = nActivePlayers;
        this.loser = loser;
    }

    public int getNActivePlayers() {
        return nActivePlayers;
    }

    public ActorRef getLoser() {
        return loser;
    }
}

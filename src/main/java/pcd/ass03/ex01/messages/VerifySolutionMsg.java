package pcd.ass03.ex01.messages;


import akka.actor.ActorRef;

/**
 * Arrivata una combinazione finale, l'arbitro chiede a ogni giocatore con questo
 * messaggio se la combinazione è giusta oppure no
 */

public final class VerifySolutionMsg implements Message {
    private final ActorRef winnerRef;

    public VerifySolutionMsg(final ActorRef winnerRef){
        this.winnerRef = winnerRef;
    }

    public ActorRef getWinnerRef(){
        return winnerRef;
    }
}

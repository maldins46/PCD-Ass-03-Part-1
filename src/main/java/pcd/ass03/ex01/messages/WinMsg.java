package pcd.ass03.ex01.messages;

import akka.actor.ActorRef;

/**
 * Se il giocatore vince, i player alla ricezione si spengono
 */


public final class WinMsg implements Message{
    private final ActorRef winnerRef;

    public WinMsg(final ActorRef winnerRef){
        this.winnerRef = winnerRef;
    }

    public ActorRef getWinnerRef(){
        return winnerRef;
    }
}

package pcd.ass03.ex01.messages;


import akka.actor.ActorRef;
import pcd.ass03.ex01.utils.Combination;

/**
 * Arrivata una combinazione finale, l'arbitro chiede a ogni giocatore con questo
 * messaggio se la combinazione è giusta oppure no
 */

public final class VerifySolutionMsg implements Message {
    private final ActorRef sender;
    private final Combination combination;

    public VerifySolutionMsg(final ActorRef sender, final Combination combination){
        this.sender = sender;
        this.combination = combination;
    }

    public ActorRef getSender() {
        return sender;
    }

    public Combination getCombination() {
        return combination;
    }
}

package pcd.ass03.ex01.messages;


import akka.actor.ActorRef;
import pcd.ass03.ex01.utils.Combination;

/**
 * Arrivata una combinazione finale, l'arbitro chiede a ogni giocatore con questo
 * messaggio se la combinazione è giusta oppure no
 */

public final class VerifySolutionMsg implements Message {
    private final Combination combination;

    public VerifySolutionMsg(final Combination combination){
        this.combination = combination;
    }

    public Combination getCombination() {
        return combination;
    }
}

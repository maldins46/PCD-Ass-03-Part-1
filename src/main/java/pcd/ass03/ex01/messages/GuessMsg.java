package pcd.ass03.ex01.messages;

import akka.actor.ActorRef;
import pcd.ass03.ex01.utils.Combination;


/**
 * Chiede a un giocatore quanto una sequenza scelta si avvicina a quella giusta.
 */
public final class GuessMsg implements Message {
    private final Combination combination;

    private final ActorRef sender;

    public GuessMsg(final Combination combination, final ActorRef sender) {
        this.sender = sender;
        this.combination = combination;
    }

    public ActorRef getSender(){ return sender; }

    public Combination getCombination() {
        return combination;
    }
}

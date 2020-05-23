package pcd.ass03.ex01.messages;

import akka.actor.ActorRef;

/**
 * Risposta all'arbitro riguardo la validità della combinazione nel tentativo di vitt.
 */

public final class VerifySolutionResponseMsg implements Message {
    private final ActorRef sender;
    private final boolean solutionGuessed;

    public VerifySolutionResponseMsg(final ActorRef sender, final boolean solutionGuessed){
        this.sender = sender;

        this.solutionGuessed = solutionGuessed;
    }

    public ActorRef getSender() {
        return sender;
    }

    public boolean isSolutionGuessed() {
        return solutionGuessed;
    }
}

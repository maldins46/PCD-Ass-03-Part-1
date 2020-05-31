package pcd.ass03.ex01.messages;

import akka.actor.ActorRef;

/**
 * Risposta all'arbitro riguardo la validità della combinazione nel tentativo di vitt.
 */

public final class VerifySolutionResponseMsg implements Message {
    private final boolean solutionGuessed;

    public VerifySolutionResponseMsg(final boolean solutionGuessed){
        this.solutionGuessed = solutionGuessed;
    }

    public boolean isSolutionGuessed() {
        return solutionGuessed;
    }
}

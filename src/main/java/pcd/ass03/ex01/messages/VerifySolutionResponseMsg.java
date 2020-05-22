package pcd.ass03.ex01.messages;

import pcd.ass03.ex01.utils.Combination;

/**
 * Risposta all'arbitro riguardo la validità della combinazione nel tentativo di vitt.
 */

public final class VerifySolutionResponseMsg implements Message {
    private final Combination supposedCombination;

    public VerifySolutionResponseMsg(final Combination supposedCombination){
        this.supposedCombination = supposedCombination;
    }

    public Combination getSupposedCombination(){
        return supposedCombination;
    }
}

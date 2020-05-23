package pcd.ass03.ex01.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


final class CombinationImpl implements Combination {

    private final List<Integer> combination;

    /**
     * This constructor generates a combination with random values for each cypher.
     * @param combinationSize the number of cyphers of the combination.
     */
    CombinationImpl(final int combinationSize) {
        final Random random = new Random();

        this.combination = new ArrayList<>();
        for (int i = 0; i < combinationSize; i++) {
            this.combination.add(random.nextInt(COMBINATION_RANGE));
        }
    }


    @Override
    public boolean compare(final Combination toCompare) {
        return combination.equals(toCompare.getCombination());
    }

    @Override
    public int computeGuessedPositions(final Combination toCompare) {
        int nOfGuessedPositions = 0;

        if (combWithDifferentSize(toCompare)) {
            return nOfGuessedPositions;
        }

        for (int i = 0; i < combination.size(); i++) {
            if (combination.get(i).equals(toCompare.getCombination().get(i))) {
                nOfGuessedPositions++;
            }
        }
        return nOfGuessedPositions;
    }

    @Override
    public int computeGuessedCyphers(final Combination toCompare) {
        int nOfGuessedCyphers = 0;

        if (combWithDifferentSize(toCompare)) {
            return nOfGuessedCyphers;
        }

        for (int i = 0; i < combination.size(); i++) {
            if (combination.contains(toCompare.getCombination().get(i))
                    && !combination.get(i).equals(toCompare.getCombination().get(i))) {
                nOfGuessedCyphers++;
            }
        }
        return nOfGuessedCyphers;
    }

    private boolean combWithDifferentSize(final Combination toCompare) {
        return combination.size() != toCompare.getCombination().size();
    }

    public int getCombinationSize(){
        return this.combination.size();
    }

    public List<Integer> getCombination(){ return combination;}
}

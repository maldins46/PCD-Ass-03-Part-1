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
    public boolean compare(final CombinationImpl toCompare) {
        return combination.equals(toCompare.combination);
    }

    @Override
    public int computeGuessedPositions(final CombinationImpl toCompare) {
        int nOfGuessedPositions = 0;

        if (combWithDifferentSize(toCompare)) {
            return nOfGuessedPositions;
        }

        for (int i = 0; i < combination.size(); i++) {
            if (combination.get(i).equals(toCompare.combination.get(i))) {
                nOfGuessedPositions++;
            }
        }
        return nOfGuessedPositions;
    }

    @Override
    public int computeGuessedCyphers(final CombinationImpl toCompare) {
        int nOfGuessedCyphers = 0;

        if (combWithDifferentSize(toCompare)) {
            return nOfGuessedCyphers;
        }

        for (int i = 0; i < combination.size(); i++) {
            if (combination.contains(toCompare.combination.get(i))
                    && !combination.get(i).equals(toCompare.combination.get(i))) {
                nOfGuessedCyphers++;
            }
        }
        return nOfGuessedCyphers;
    }

    private boolean combWithDifferentSize(final CombinationImpl toCompare) {
        return combination.size() != toCompare.combination.size();
    }
}

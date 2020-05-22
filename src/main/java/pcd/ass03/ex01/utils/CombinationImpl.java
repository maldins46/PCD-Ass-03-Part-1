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

    /**
     * Checks whether the passed combination is equal to the current.
     * @param toCompare the combination to compare with the current.
     * @return true if the combination are equals, false otherwise.
     */
    @Override
    public boolean compare(final CombinationImpl toCompare) {
        return combination.equals(toCompare.combination);
    }

    /**
     * Returns the number of cyphers that are in the same place, with the same value.
     * @param toCompare the combination to compare with the current.
     * @return the number of cyphers that are in the same place, with the same value.
     *         If the size of the lists are different, it returns 0.
     */
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

    /**
     * Returns the number of cyphers that are in common between combination, but NOT
     * in the same place.
     * @param toCompare the combination to compare with the current.
     * @return the number of cyphers that are in common between combination, but NOT
     *         in the same place. If the size of the lists are different, it returns 0.
     */
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

    /**
     * Checks whether combinations have the same size.
     * @param toCompare the combination to compare with the current.
     * @return true if combinations have the same size, false otherwise.
     */
    private boolean combWithDifferentSize(final CombinationImpl toCompare) {
        return combination.size() != toCompare.combination.size();
    }
}

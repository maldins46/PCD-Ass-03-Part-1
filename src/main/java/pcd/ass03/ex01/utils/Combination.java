package pcd.ass03.ex01.utils;
import java.util.List;

/**
 * This class represents a generic combination, generated by a player.
 */
public interface Combination {
    /**
     * It characterizes the range of integers used to represent an element of the
     * combination. Using decimal cyphers from 0 to 9, the range is 10.
     */
    int COMBINATION_RANGE = 10;

    /**
     * Checks whether the passed combination is equal to the current.
     * @param toCompare the combination to compare with the current.
     * @return true if the combination are equals, false otherwise.
     */
    boolean compare(Combination toCompare);

    /**
     * Returns the number of cyphers that are in the same place, with the same value.
     * @param toCompare the combination to compare with the current.
     * @return the number of cyphers that are in the same place, with the same value.
     *         If the size of the lists are different, it returns 0.
     */
    int computeGuessedPositions(Combination toCompare);

    /**
     * Returns the number of cyphers that are in common between combination, but NOT
     * in the right place.
     * @param toCompare the combination to compare with the current.
     * @return the number of cyphers that are in common between combination, but NOT
     *         in the same place. If the size of the lists are different, it returns 0.
     */
    int computeGuessedCyphers(Combination toCompare);


    /**
     * Factory to generate a random combination.
     * @param combinationSize the number of chyphers of the combination
     * @return the new combination
     */
    static Combination of(final int combinationSize) {
        return new CombinationImpl(combinationSize);
    }

    static Combination of(final List<Integer> combination) {
        return new CombinationImpl(combination);
    }

    /**
     * This method returns the size of the combination.
     * @return the size
     */
    int getCombinationSize();

    /**
     * This method returns the combination.
     * @return the combination
     */
    List<Integer> getCombination();

}

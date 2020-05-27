package pcd.ass03.ex01.messages;

/**
 * Notify to referee to start the game.
 * Referee create players.
 */
public final class StartGameMsg implements Message {

    // TODO Riferimento alla Gui del human player.

    /**
     * Number of player.
     */
    private final int nPlayers;


    /**
     * Size of the combination.
     */
    private final int combinationSize;


    /**
     * Check if in the game is present a human player.
     */
    private final boolean humanPlayer;


    /**
     * Check if a guess' response have to be sended only to the guessed player.
     */
    private final boolean responseOnlyToSender;


    /**
     * Constructor for StartGameMsg.
     * @param nPlayers is number of the player.
     * @param combinationSize is the size of combination.
     * @param humanPlayer check if is present a human player.
     * @param responseOnlyToSender check if a guess' response have to be sended only to the guessed player.
     */
    public StartGameMsg(final int nPlayers, final int combinationSize, final boolean humanPlayer, final boolean responseOnlyToSender) {
        this.nPlayers = nPlayers;
        this.combinationSize = combinationSize;
        this.humanPlayer = humanPlayer;
        this.responseOnlyToSender = responseOnlyToSender;
    }

    /**
     * Getter for number of player.
     * @return player's number.
     */
    public int getnPlayers() {
        return nPlayers;
    }


    /**
     * Getter for size of combination.
     * @return size of combination.
     */
    public int getCombinationSize() {
        return combinationSize;
    }


    /**
     * Getter that check if is present a human player.
     * @return true if a human player is present, false otherwise.
     */
    public boolean getHumanPlayer() {
        return humanPlayer;
    }


    /**
     * Getter that check if response have to be sended at all or only the sender player.
     * @return if a guess' response have to be sended only to the guessed player or at all.
     */
    public boolean isResponseOnlyToSender() {
        return responseOnlyToSender;
    }
}

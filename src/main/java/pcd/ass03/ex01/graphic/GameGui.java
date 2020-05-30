package pcd.ass03.ex01.graphic;

import pcd.ass03.ex01.actors.GuiActor;
import pcd.ass03.ex01.messages.Message;

/**
 * Gives a rapresantation of a gui to configurate the match, and to follow the
 * progress of it, using a label.
 */
public interface GameGui {

    /**
     * Shows the Gui into the screen.
     */
    void launch();

    /**
     * Prints a log into the frame label.
     * @param logMsg is the message to print.
     */
    void printLog(String logMsg);

    /**
     * Indicates if a player won or if all players lost.
     * @param msg WinMsg, if a player won, or LoseMsg, if all players lost.
     */
    void finishMatch(Message msg);

    /**
     * Gets the seleted number of players in the match.
     * @return the selected value.
     */
    int getNPlayers();


    /**
     * Gets the selected combination size.
     * @return the selected value.
     */
    int getCombSize();


    /**
     * Gets whether the game will let the user play.
     * @return the selected value.
     */
    boolean hasHumanPlayer();


    /**
     * Gets whether to enable the optional behavior to respond only to the guesser.
     * @return the selected value.
     */
    boolean respondsOnlyToTheGuesser();


    /**
     * Factory to create the Gui.
     * @param guiActor reference the corresponding actor.
     * @return an implementation of GameGui.
     */
    static GameGui of(GuiActor guiActor) {
        return new GameGuiImpl(guiActor);
    }

}



package pcd.ass03.ex01.graphic;

import pcd.ass03.ex01.actors.GuiActor;
import pcd.ass03.ex01.messages.Message;

public interface GameGui {

    int HEIGHT = 30;
    int WEIGHT = 500;

    /**
     * Message log printed into a label.
     * @param logMsg is the message to print.
     */
    void update(String logMsg);

    /**
     * Indicates if a player have won or if all players have lost.
     * @param msg could be WinMsg if a player have won or
     *           LoseMsg if all player have lost.
     */
    void finish(Message msg);

    /**
     * Factory to create a Gui.
     * @param guiActor reference to his actor.
     * @return an implementation of GameGui.
     */
    static GameGui of(GuiActor guiActor) {
        return new GameGuiImpl(guiActor);
    }

}



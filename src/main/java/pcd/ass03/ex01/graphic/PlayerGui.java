package pcd.ass03.ex01.graphic;

import akka.actor.ActorRef;
import pcd.ass03.ex01.actors.HumanPlayerActor;
import pcd.ass03.ex01.actors.PlayerActor;
import pcd.ass03.ex01.utils.Combination;

import java.util.List;

public interface PlayerGui {

    void launch();

    void loseMatch();

    void finishMatch(String winnerName, boolean youWin);

    void startTurn();

    void startFinalTurnPart(Combination guessResponse);


    static PlayerGui of(HumanPlayerActor player, List<ActorRef> enemies, int combSize) {
        return new PlayerGuiImpl(player, enemies, combSize);
    }
}

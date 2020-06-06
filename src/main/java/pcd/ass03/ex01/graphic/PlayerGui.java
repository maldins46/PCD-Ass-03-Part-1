package pcd.ass03.ex01.graphic;

import akka.actor.ActorRef;
import pcd.ass03.ex01.actors.HumanPlayerActor;
import pcd.ass03.ex01.actors.PlayerActor;
import pcd.ass03.ex01.utils.Combination;

import java.util.List;

public interface PlayerGui {

    void launch();

    void matchLoss(boolean wrongSolution);

    void matchVictory(ActorRef winnerName);

    void startTurn();

    void startFinalTurnPart(final int guessedPos, final int guessedCyphers);

    void timeoutReceived();


    static PlayerGui of(HumanPlayerActor player, List<ActorRef> enemies, int combSize) {
        return new PlayerGuiImpl(player, enemies, combSize);
    }
}

package pcd.ass03.ex01.actors;

import akka.actor.ActorRef;
import pcd.ass03.ex01.graphic.PlayerGui;
import pcd.ass03.ex01.messages.*;
import pcd.ass03.ex01.utils.Combination;

import java.util.stream.Collectors;

public class HumanPlayerActor extends PlayerActor {
    private PlayerGui playerGui;

    @Override
    protected void handleGuessResponseMsg(GuessResponseMsg guessRespMsg) {
        super.handleGuessResponseMsg(guessRespMsg);
    }

    @Override
    protected void handleLoseMsg(LoseMsg loseMsg) {
        super.handleLoseMsg(loseMsg);
    }

    @Override
    protected void handleStartGameMsg(StartPlayerMsg startPlayerMsg) {
        super.handleStartGameMsg(startPlayerMsg);
        playerGui = PlayerGui.of(this, enemies);
    }

    @Override
    protected void handleStartTurnMsg(StartTurnMsg startTurnMsg) {
        super.handleStartTurnMsg(startTurnMsg);
    }

    @Override
    protected void handleStopGameMsg(StopGameMsg stopGameMsg) {
        super.handleStopGameMsg(stopGameMsg);
    }

    @Override
    protected void handleTimoutMsg(TimeoutMsg timeoutMsg) {
        super.handleTimoutMsg(timeoutMsg);
    }

    @Override
    protected void handleWinMsg(WinMsg winMsg) {
        super.handleWinMsg(winMsg);
    }


    public void sendGuessMessage(final ActorRef receiver, final Combination guess) {
        receiver.tell(new GuessMsg(guess), getSelf());
    }
}

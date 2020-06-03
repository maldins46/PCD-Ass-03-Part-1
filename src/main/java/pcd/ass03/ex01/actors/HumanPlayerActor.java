package pcd.ass03.ex01.actors;

import pcd.ass03.ex01.graphic.PlayerGui;
import pcd.ass03.ex01.messages.*;

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
}

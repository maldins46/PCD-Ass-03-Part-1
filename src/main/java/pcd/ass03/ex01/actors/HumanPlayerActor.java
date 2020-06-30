package pcd.ass03.ex01.actors;

import akka.actor.ActorRef;
import pcd.ass03.ex01.graphic.PlayerGui;
import pcd.ass03.ex01.messages.*;
import pcd.ass03.ex01.utils.Combination;

import java.util.Map;

public final class HumanPlayerActor extends PlayerActor {
    private PlayerGui playerGui;

    @Override
    protected void handleGuessResponseMsg(final GuessResponseMsg guessRespMsg) {
        if (isMyTurn) {
            playerGui.startFinalTurnPart(guessRespMsg.getGuessedPositions(), guessRespMsg.getGuessedCyphers());
        }
    }


    @Override
    protected void handleStartGameMsg(final StartPlayerMsg startPlayerMsg) {
        super.handleStartGameMsg(startPlayerMsg);
        playerGui = PlayerGui.of(this, enemies, startPlayerMsg.getCombinationSize());
        playerGui.launch();
    }


    @Override
    protected void handleStartTurnMsg(final StartTurnMsg startTurnMsg) {
        getContext().become(turnBehavior());
        isMyTurn = true;
        playerGui.startTurn();
    }


    @Override
    protected void handleStopGameMsg(final StopGameMsg stopGameMsg) {
        super.handleStopGameMsg(stopGameMsg);
        playerGui.matchLoss(false);
    }


    @Override
    protected void handleTimeoutMsg(final TimeoutMsg timeoutMsg) {
        super.handleTimeoutMsg(timeoutMsg);
        playerGui.timeoutReceived();
    }


    @Override
    protected void handleLoseMsg(final LoseMsg loseMsg) {
        super.handleLoseMsg(loseMsg);
        if (loseMsg.getNActivePlayers() == 0) {
            playerGui.matchLoss(false);

        } else if (loseMsg.getLoser().equals(getSelf())) {
            playerGui.matchLoss(true);
        }
    }


    @Override
    protected void handleWinMsg(final WinMsg winMsg) {
        super.handleWinMsg(winMsg);
        playerGui.matchVictory(winMsg.getWinner());
        getContext().become(lostBehavior());
    }


    public void sendGuessMsg(final ActorRef receiver, final Combination guess) {
        receiver.tell(new GuessMsg(guess), getSelf());
    }


    public void sendFinishTurnMsg() {
        arbiter.tell(new FinishTurnMsg(), getSelf());
        isMyTurn = false;
        getContext().become(defaultBehavior());
    }


    public void sendSolutionMsg(final Map<ActorRef, Combination> solution) {
        arbiter.tell(new SolutionMsg(solution), getSelf());
        getContext().become(defaultBehavior());
        isMyTurn = false;
    }
}

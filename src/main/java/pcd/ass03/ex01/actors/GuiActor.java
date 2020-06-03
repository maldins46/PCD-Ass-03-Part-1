package pcd.ass03.ex01.actors;

import akka.actor.AbstractActor;
import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pcd.ass03.ex01.graphic.GameGui;
import pcd.ass03.ex01.messages.*;

/**
 * Actor that interact with referee and update GameGui.
 */
public final class GuiActor extends AbstractActor {

    /**
     * Reference to Gui's game.
     */
    private GameGui gameGui;

    /**
     * Reference to referee.
     */
    private ActorRef referee;

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), getSelf().path().name());

    /**
     * During the pre-start, the concrete gui is created and shown to the user,
     * and the referee actor is created (but not initialized).
     * The initialization of the actor is done after the user presses start.
     */
    @Override
    public void preStart() {
        this.gameGui = GameGui.of(this);
        this.gameGui.launch();
    }


    @Override
    public Receive createReceive() {
        // the standard behavior of the actor
        return receiveBuilder()
                .match(LogMsg.class, this::handleLogMsg)
                .match(WinMsg.class, this::handleWinMsg)
                .match(LoseMsg.class, this::handleLoseMsg)
                .matchAny(msg -> log.error("Message not recognized: " + msg))
                .build();
    }


    /**
     * Send start game message to referee, that also initializes the players.
     */
    public void sendStartGameMessage() {
        this.referee = getContext().getSystem().actorOf(Props.create(RefereeActor.class), "Referee");
        final StartGameMsg startGameMsg = new StartGameMsg(
                getSelf(),
                gameGui.getNPlayers(),
                gameGui.getCombSize(),
                gameGui.hasHumanPlayer(),
                gameGui.respondsOnlyToTheGuesser()
        );
        this.referee.tell(startGameMsg, getSelf());
    }


    /**
     * Sena a stog game message to the referee, aborting the execution of it.
     */
    public void sendStopGameMessage() {
        this.referee.tell(new StopGameMsg(), getSelf());
    }


    /**
     * Handle to update Gui with the game winner.
     * @param winMsg message from RefereeActor
     */
    private void handleWinMsg(final WinMsg winMsg) {
        this.gameGui.finishMatch(winMsg);
    }


    /**
     * Handle to update Gui when all players lost.
     * @param loseMsg message from RefereeActor.
     */
    private void handleLoseMsg(final LoseMsg loseMsg) {
        if (loseMsg.getNActivePlayers() == 0) {
            this.gameGui.finishMatch(loseMsg);
        }
    }


    /*
     * This method will send a message to the GameGui.
     * Message is an update of what happen in the game.
     * @param logMsg.
     */
    private void handleLogMsg(final LogMsg logMsg) {
        this.gameGui.printLog(logMsg.getLog());
    }

}

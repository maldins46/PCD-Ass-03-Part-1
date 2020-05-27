package pcd.ass03.ex01.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import pcd.ass03.ex01.graphic.GameGui;
import pcd.ass03.ex01.messages.LogMsg;
import pcd.ass03.ex01.messages.LoseMsg;
import pcd.ass03.ex01.messages.StartGameMsg;
import pcd.ass03.ex01.messages.WinMsg;

/**
 * Actor that receive referee message and update Gui.
 */
public final class GuiActor extends GenericActor {

    /**
     * Reference to Gui's game.
     */
    private GameGui gameGui;

    /**
     * Reference to referee.
     */
    private ActorRef refereeActor;

    @Override
    public void preStart() {
        this.gameGui = GameGui.of(this);
        this.refereeActor = getContext().getSystem().actorOf(Props.create(RefereeActor.class), "Referee");
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(LogMsg.class, this::handleLogMsg)
                .match(WinMsg.class, this::handleWinMsg)
                .match(LoseMsg.class, this::handleLoseMsg)
                .matchAny(this::messageNotRecognized)
                .build();
    }

    /**
     * Send start Game to referee.
     * @param nPlayers number of player in the game.
     * @param combinationSize length of cypher.
     */
    public void sendStartGame(final int nPlayers, final int combinationSize) {
        this.refereeActor.tell(new StartGameMsg(getSelf(), nPlayers, combinationSize), getSelf());
    }


    /**
     * Handle to update Gui with the game winner.
     * @param winMsg message from RefereeActor
     */
    private void handleWinMsg(final WinMsg winMsg) {
        this.gameGui.finish(winMsg);
    }

    /**
     * Handle to update Gui with the game winner.
     * @param loseMsg message from RefereeActor.
     */
    private void handleLoseMsg(final LoseMsg loseMsg) {
        if (loseMsg.getNActivePlayers() == 0) {
            this.gameGui.finish(loseMsg);
        } else {
            // fixme check how to print his name
            this.handleLogMsg(new LogMsg("Player " + loseMsg.getLoser() + " have lost!"));
        }
    }


    /*
     * This method will send a message to the GameGui.
     * Message is an update of what happen in the game.
     * @param logMsg.
     */
    private void handleLogMsg(final LogMsg logMsg) {
        this.gameGui.update(logMsg.getLog());
    }

}

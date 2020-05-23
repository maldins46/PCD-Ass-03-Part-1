package pcd.ass03.ex01.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pcd.ass03.ex01.messages.*;

import java.util.*;

/**
 * Represents the entity that coordinates actions inside the match
 */
public final class RefereeActor extends GenericActor {
    /**
     * Embedded Akka logger used to show information of the execution.
     */
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    /**
     * It memorizes all players.
     */
    private final Set<ActorRef> players;

    /**
     * It memorized all not-excluded players.
     */
    private final Set<ActorRef> activePlayers;

    /**
     * Keeps track of the current 'turn lap'; for every turn, a player will
     * be removed from the list. When the is empty, it will be re-initialized randomly.
     */
    private final List<ActorRef> currentLap;

    private final Map<ActorRef, Boolean> verifySolution;


    public RefereeActor() {
        this.players = new HashSet<>();
        this.activePlayers = new HashSet<>();
        this.currentLap = new ArrayList<>();
        this.verifySolution = new HashMap<>();
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartGameMsg.class, this::handleStartGameMsg)
                .match(StopGameMsg.class, this:: handleStopGameMsg)
                .match(FinishTurnMsg.class, this::handleFinishTurnMsg)
                .match(SolutionMsg.class, this::handleSolutionMsg)
                //.match(VerifySolutionMsg.class, this::handleVerifySolutionResponseMsg)
                .matchAny(this::messageNotRecognized)
                .build();
    }


    /**
     * When a startGameMsg arrives to the referee, this actor will initialize the players,
     * and it will launch the game.
     * @param startGameMsg the message
     */
    private void handleStartGameMsg(final StartGameMsg startGameMsg) {
        ActorSystem system = this.getContext().getSystem();

        for (int i = 0; i < startGameMsg.getnPlayers(); i++) {
            final String playerName = "Player" + i;
            final ActorRef newPlayer = system.actorOf(Props.create(PlayerActor.class), playerName);
            players.add(newPlayer);
        }

        players.forEach(player -> {
            final StartPlayerMsg message = new StartPlayerMsg(startGameMsg.getCombinationSize(), players, getSelf());
            player.tell(message, getSelf());
        });
        startNextTurn();
    }


    /**
     * When the referee receives a StopGameMsg from the Gui, it sends a stop message to
     * the players and it stops himself.
     * @param stopGameMsg the received message
     */
    private void handleStopGameMsg(final StopGameMsg stopGameMsg) {
        players.forEach(player -> {
            final StopGameMsg message = new StopGameMsg();
            player.tell(message, getSelf());
        });

        this.context().stop(getSelf());
    }


    /**
     * When the referee receives a FinishTurnMessage from a player, it starts the
     * next turn.
     * @param finishTurnMsg the received message.
     */
    private void handleFinishTurnMsg(final FinishTurnMsg finishTurnMsg) {
        startNextTurn();
    }


    /**
     * When the referee receives a SolutionMsg from a player, it queries all players
     * to know whether the solution is right.
     * @param solutionMsg the received message
     */
    private void handleSolutionMsg(final SolutionMsg solutionMsg) {
        players.forEach(player -> {
            final VerifySolutionMsg message = new VerifySolutionMsg(
                    solutionMsg.getSender(),
                    solutionMsg.getCombinations().get(player)
            );
            player.tell(message, getSelf());
        });
    }


    /**
     * It handles the initialization of the currentLap, if necessary, and it starts the
     * next turn.
     */
    private void startNextTurn() {
        // initializes the lap, if necessary
        if (currentLap.isEmpty()) {
            currentLap.addAll(activePlayers);
            Collections.shuffle(currentLap);
        }

        currentLap.remove(0).tell(new StartTurnMsg(), getSelf());
    }
}

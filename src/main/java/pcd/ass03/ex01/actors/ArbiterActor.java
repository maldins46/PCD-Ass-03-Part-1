package pcd.ass03.ex01.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pcd.ass03.ex01.messages.*;

import java.time.Duration;
import java.util.*;

/**
 * Represents the entity that coordinates actions inside the match.
 */
public final class ArbiterActor extends AbstractActor {
    private static final Duration TURN_DURATION = Duration.ofSeconds(60);

    /**
     * Reference to the actor used to handle the gui. The reference is
     * necessary to send log.debug, win, and lost messages.
     */
    private ActorRef guiActor;

    /**
     * It memorizes references to all players.
     */
    private final Set<ActorRef> players = new HashSet<>();

    /**
     * It memorizes only players that have not been excluded by the game,
     * subsequently to a wrong attempt to give the final solution.
     */
    private final Set<ActorRef> activePlayers = new HashSet<>();

    /**
     * Keeps track of the current 'turn lap'; for every turn, a player
     * will be removed from the list. When thet is empty, it will be
     * re-initialized randomly.
     */
    private final List<ActorRef> currentLap = new ArrayList<>();

    /**
     * Reference to the current turn player.
     */
    private ActorRef currentTurnPlayer;

    /**
     * It memorizes the reference to tha player that submitted a solution,
     * during the verification process.
     */
    private ActorRef solutionSubmitter;

    /**
     * It memorizes reference and result of the verification process for
     * a solution, relative to every player examined.
     */
    private final Map<ActorRef, Boolean> solutionResults = new HashMap<>();

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), getSelf().path().name());


    @Override
    public Receive createReceive() {
        return startBehavior();
    }


    /**
     * Returns the initial behavior of the referee. Initially, it can
     * only receive a start message (or force stop messages).
     * @return The initial behavior of the referee.
     */
    private Receive startBehavior() {
        return receiveBuilder()
                .match(StartGameMsg.class, this::handleStartGameMsg)
                .match(StopGameMsg.class, this:: handleStopGameMsg)
                .matchAny(msg -> log.error("Message not recognized: " + msg))
                .build();
    }


    /**
     * By default, the actor is waiting for the end of a specific
     * turn. In other words, it can only receive finish turn messages,
     * or solution messages (or force stop messages).
     * @return The default behavior of the referee.
     */
    private Receive defaultBehavior() {
        return receiveBuilder()
                .match(FinishTurnMsg.class, this::handleFinishTurnMsg)
                .match(SolutionMsg.class, this::handleSolutionMsg)
                .match(ReceiveTimeout.class, this::handlePlayerReceiveTimeout)
                .match(StopGameMsg.class, this:: handleStopGameMsg)
                .matchAny(msg -> log.error("Message not recognized: " + msg))
                .build();
    }


    /**
     * This is a particular behavior of the referee, used when it
     * has to verify a solution. In this stage, the referee can only
     * receive verify solution responses (or force stop messages).
     * @return The behavior during a solution verification.
     */
    private Receive verifySolutionBehavior() {
        return receiveBuilder()
                .match(VerifySolutionResponseMsg.class, this::handleVerifySolutionResponseMsg)
                .match(StopGameMsg.class, this:: handleStopGameMsg)
                .matchAny(msg -> log.error("Message not recognized: " + msg))
                .build();
    }


    /**
     * When a StartGameMsg arrives to the referee from the gui, the referee
     * initializes the players, and launches the game.
     * @param startGameMsg the received message.
     */
    private void handleStartGameMsg(final StartGameMsg startGameMsg) {
        getContext().become(defaultBehavior());

        final ActorSystem system = this.getContext().getSystem();

        for (int i = 0; i < startGameMsg.getnPlayers(); i++) {
            final String playerName = "Player" + i;
            final Props playerProps;
            if (startGameMsg.getHumanPlayer() && i == 0) {
                playerProps = Props.create(HumanPlayerActor.class);
            } else {
                playerProps = Props.create(PlayerActor.class);
            }

            final ActorRef newPlayer = system.actorOf(playerProps, playerName);
            players.add(newPlayer);

        }

        guiActor = startGameMsg.getGuiActor();
        activePlayers.addAll(players);
        players.forEach(player -> {
            final StartPlayerMsg message = new StartPlayerMsg(
                    startGameMsg.getCombinationSize(),
                    players,
                    getSelf(),
                    startGameMsg.isResponseOnlyToSender()
            );
            player.tell(message, getSelf());
        });

        log.debug("Referee started, starting players");
        startNextTurn();
    }


    /**
     * When the referee receives a StopGameMsg from the Gui, it sends a stop
     * message to the players and it stops himself.
     * @param stopGameMsg the received message.
     */
    private void handleStopGameMsg(final StopGameMsg stopGameMsg) {
        log.debug("Received stopMsg, aborting " + getSelf().path().name());
        players.forEach(player -> {
            player.tell(new StopGameMsg(), getSelf());
        });

        getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
    }

    /**
     * This message is self-send to the actor, when the player do not respond
     * with an end turn message (or a solution message) after a given time.
     * It gives the turn to another player.
     * @param receiveTimeout the received message.
     */
    private void handlePlayerReceiveTimeout(final ReceiveTimeout receiveTimeout) {
        currentTurnPlayer.tell(new TimeoutMsg(), getSelf());
        startNextTurn();
    }


    /**
     * When the referee receives a FinishTurnMessage from a player, it
     * starts the next turn.
     * @param finishTurnMsg the received message.
     */
    private void handleFinishTurnMsg(final FinishTurnMsg finishTurnMsg) {
        log.debug("Turn finished, starting a new one");
        startNextTurn();
    }


    /**
     * When the referee receives a SolutionMsg from a player, it queries
     * all players (except the verified player) to know whether the solution
     * is right. It also changes the behavior of the actor, by permitting it
     * to receive only messages of type verifySolutionResponse.
     * @param solutionMsg the received message.
     */
    private void handleSolutionMsg(final SolutionMsg solutionMsg) {
        log.debug("Someone sent a solution, start verifying");
        getContext().become(verifySolutionBehavior());

        solutionSubmitter = getSender();
        players.stream().filter(player -> player != solutionSubmitter).forEach(player -> {
            final VerifySolutionMsg message = new VerifySolutionMsg(solutionMsg.getCombinations().get(player));
            player.tell(message, getSelf());
        });
    }


    /**
     * When the referee receives VerifySolutionResponseMsg from a player,
     * it memorizes the result; then, if all players have responded,
     * it communicate the result to the verified player. It also removes
     * the behavior, if necessary.
     * @param verifySolRespMsg the received message.
     */
    private void handleVerifySolutionResponseMsg(final VerifySolutionResponseMsg verifySolRespMsg) {
        log.debug("Player responded to a verify solution msg");
        solutionResults.put(getSender(), verifySolRespMsg.isSolutionGuessed());

        // this means that all solution results have been received
        if (solutionResults.size() == players.size() - 1) {
            if (isSolutionWinner()) {
                // in this case, the verified player won. Notify this to all players.
                log.debug("Verified player won! Finishing match");
                final WinMsg winMsg = new WinMsg(solutionSubmitter);
                guiActor.tell(winMsg, getSelf());
                players.forEach(player -> player.tell(winMsg, getSelf()));
                finishMatch();

            } else {
                // in this case, the verified player lost. Remove it from the
                // active players and notify it to all
                log.debug("Verified player lost! Continue, if necessary");
                activePlayers.remove(solutionSubmitter);
                final LoseMsg loseMsg = new LoseMsg(activePlayers.size(), solutionSubmitter);
                guiActor.tell(loseMsg, getSelf());
                players.forEach(player -> player.tell(loseMsg, getSelf()));

                if (activePlayers.size() > 0) {
                    // re-initializes structures and behaviors
                    solutionSubmitter = null;
                    solutionResults.clear();
                    getContext().become(defaultBehavior());
                    startNextTurn();

                } else {
                    finishMatch();
                }
            }
        }
    }

    private void finishMatch() {
        // finish
        getContext().cancelReceiveTimeout();
        log.debug("Match finished, aborting " + getSelf().path().name());
        getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
    }


    /**
     * Checks whether all the collected solution results are right.
     * @return True if all the collected solution results are
     *         right, false otherwise.
     */
    private boolean isSolutionWinner() {
        for (final ActorRef player : solutionResults.keySet()) {
            if (!solutionResults.get(player)) {
                return false;
            }
        }
        return true;
    }


    /**
     * It handles the initialization of the currentLap, if necessary,
     * and it starts the next turn.
     */
    private void startNextTurn() {
        if (currentLap.isEmpty()) {
            currentLap.addAll(activePlayers);
            Collections.shuffle(currentLap);
        }

        currentTurnPlayer = currentLap.remove(0);
        currentTurnPlayer.tell(new StartTurnMsg(), getSelf());

        getContext().cancelReceiveTimeout();
        getContext().setReceiveTimeout(TURN_DURATION);
    }
}

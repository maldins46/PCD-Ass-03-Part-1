package pcd.ass03.ex01.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import pcd.ass03.ex01.messages.*;

import java.util.*;

/**
 * Represents the entity that coordinates actions inside the match.
 */
public final class RefereeActor extends GenericActor {

    /**
     * It memorizes references to all players.
     */
    private final Set<ActorRef> players;

    /**
     * It memorizes only players that have not been excluded by the game,
     * subsequently to a wrong attempt to give the final solution.
     */
    private final Set<ActorRef> activePlayers;

    /**
     * Keeps track of the current 'turn lap'; for every turn, a player
     * will be removed from the list. When thet is empty, it will be
     * re-initialized randomly.
     */
    private final List<ActorRef> currentLap;

    /**
     * It memorizes the reference to tha player that submitted a solution,
     * during the verification process.
     */
    private ActorRef solutionSubmitter;

    /**
     * It memorizes reference and result of the verification process for
     * a solution, relative to every player examined.
     */
    private final Map<ActorRef, Boolean> solutionResults;


    /**
     * The default constructor. It initializes data structures as empty.
     */
    public RefereeActor() {
        this.players = new HashSet<>();
        this.activePlayers = new HashSet<>();
        this.currentLap = new ArrayList<>();
        this.solutionResults = new HashMap<>();
    }


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
                .matchAny(this::messageNotRecognized)
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
                .match(StopGameMsg.class, this:: handleStopGameMsg)
                .match(FinishTurnMsg.class, this::handleFinishTurnMsg)
                .match(SolutionMsg.class, this::handleSolutionMsg)
                .matchAny(this::messageNotRecognized)
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
                .matchAny(this::messageNotRecognized)
                .build();
    }


    /**
     * When a StartGameMsg arrives to the referee from the gui, the referee
     * initializes the players, and launches the game.
     * @param startGameMsg the received message.
     */
    private void handleStartGameMsg(final StartGameMsg startGameMsg) {
        final ActorSystem system = this.getContext().getSystem();

        for (int i = 0; i < startGameMsg.getnPlayers(); i++) {
            final String playerName = "Player" + i;
            final Props playerProps = Props.create(PlayerActor.class);
            final ActorRef newPlayer = system.actorOf(playerProps, playerName);
            players.add(newPlayer);
        }

        activePlayers.addAll(players);
        players.forEach(player -> {
            final StartPlayerMsg message = new StartPlayerMsg(startGameMsg.getCombinationSize(), players, getSelf());
            player.tell(message, getSelf());
        });

        startNextTurn();
        getContext().become(defaultBehavior(), true);
    }


    /**
     * When the referee receives a StopGameMsg from the Gui, it sends a stop
     * message to the players and it stops himself.
     * @param stopGameMsg the received message.
     */
    private void handleStopGameMsg(final StopGameMsg stopGameMsg) {
        players.forEach(player -> {
            final StopGameMsg message = new StopGameMsg();
            player.tell(message, getSelf());
        });

        this.getContext().stop(getSelf());
    }


    /**
     * When the referee receives a FinishTurnMessage from a player, it
     * starts the next turn.
     * @param finishTurnMsg the received message.
     */
    private void handleFinishTurnMsg(final FinishTurnMsg finishTurnMsg) {
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
        getContext().become(verifySolutionBehavior(), true);

        solutionSubmitter = solutionMsg.getSender();
        players.stream().filter(player -> player != solutionSubmitter).forEach(player -> {
            final VerifySolutionMsg message = new VerifySolutionMsg(
                    solutionMsg.getSender(),
                    solutionMsg.getCombinations().get(player)
            );
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
        solutionResults.put(verifySolRespMsg.getSender(), verifySolRespMsg.isSolutionGuessed());

        // this means that all solution results have been received
        if (solutionResults.size() == players.size()) {
            if (isSolutionWinner()) {
                // in this case, the verified player won. Notify this to all players.
                players.forEach(player -> {
                    final WinMsg message = new WinMsg(solutionSubmitter);
                    player.tell(message, getSelf());
                });

            } else {
                // in this case, the verified player lost. Remove it from the
                // active players and notify it to all
                activePlayers.remove(solutionSubmitter);
                players.forEach(player -> {
                    final LoseMsg message = new LoseMsg(activePlayers.size(), solutionSubmitter);
                    player.tell(message, getSelf());
                });
            }

            // re-initializes structures and behaviors.
            solutionSubmitter = null;
            solutionResults.clear();
            getContext().unbecome();
        }

        if (activePlayers.size() > 0) {
            startNextTurn();
        }
    }


    /**
     * Checks whether all the collected solution results are right.
     * @return True if all the collected solution results are
     *         right, false otherwise.
     */
    private boolean isSolutionWinner() {
        boolean win = true;
        for (final ActorRef player : solutionResults.keySet()) {
            if (!solutionResults.get(player)) {
                win = false;
            }
        }
        return win;
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

        currentLap.remove(0).tell(new StartTurnMsg(), getSelf());
    }
}

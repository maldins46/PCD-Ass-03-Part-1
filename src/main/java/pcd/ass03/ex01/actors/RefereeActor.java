package pcd.ass03.ex01.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import pcd.ass03.ex01.messages.*;

import java.util.*;

/**
 * Represents the entity that coordinates actions inside the match
 */
public final class RefereeActor extends GenericActor {

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
    private ActorRef verifiedPlayer;


    public RefereeActor() {
        this.players = new HashSet<>();
        this.activePlayers = new HashSet<>();
        this.currentLap = new ArrayList<>();
        this.verifySolution = new HashMap<>();
    }


    @Override
    public Receive createReceive() {
        return standardBehavior();
    }


    private Receive standardBehavior() {
        return receiveBuilder()
                .match(StartGameMsg.class, this::handleStartGameMsg)
                .match(StopGameMsg.class, this:: handleStopGameMsg)
                .match(FinishTurnMsg.class, this::handleFinishTurnMsg)
                .match(SolutionMsg.class, this::handleSolutionMsg)
                .matchAny(this::messageNotRecognized)
                .build();
    }


    private Receive verifySolutionBehavior() {
        return receiveBuilder()
                .match(VerifySolutionResponseMsg.class, this::handleVerifySolutionResponseMsg)
                .matchAny(this::messageNotRecognized)
                .build();
    }


    /**
     * When the referee receives a SolutionMsg from a player, it queries all players
     * to know whether the solution is right.
     * @param solutionMsg the received message
     */
    void handleSolutionMsg(final SolutionMsg solutionMsg) {
        getContext().become(verifySolutionBehavior(), true);
        verifiedPlayer = solutionMsg.getSender();
        players.stream().filter(player -> player != verifiedPlayer).forEach(player -> {
            final VerifySolutionMsg message = new VerifySolutionMsg(
                    solutionMsg.getSender(),
                    solutionMsg.getCombinations().get(player)
            );
            player.tell(message, getSelf());
        });
    }


    /**
     * When a startGameMsg arrives to the referee, this actor will initialize the players,
     * and it will launch the game.
     * @param startGameMsg the message
     */
    private void handleStartGameMsg(final StartGameMsg startGameMsg) {
        final ActorSystem system = this.getContext().getSystem();

        for (int i = 0; i < startGameMsg.getnPlayers(); i++) {
            final String playerName = "Player" + i;
            final ActorRef newPlayer = system.actorOf(Props.create(PlayerActor.class), playerName);
            players.add(newPlayer);
        }

        activePlayers.addAll(players);
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

        this.getContext().stop(getSelf());
    }


    /**
     * When the referee receives VerifySolutionResponseMsg from a player, it memorizes
     * the result; then, if all players have responded, communicate the result to the players.
     * @param verifySolRespMsg the received message.
     */
    void handleVerifySolutionResponseMsg(final VerifySolutionResponseMsg verifySolRespMsg) {
        verifySolution.put(verifySolRespMsg.getSender(), verifySolRespMsg.isSolutionGuessed());

        if (verifySolution.size() == players.size()) {
            boolean win = true;
            for (ActorRef player : verifySolution.keySet()) {
                if (!verifySolution.get(player)) {
                    win = false;
                }
            }

            if (win) {
                players.forEach(player -> {
                    final WinMsg message = new WinMsg(verifiedPlayer);
                    player.tell(message, getSelf());
                });

            } else {
                activePlayers.remove(verifiedPlayer);
                players.forEach(player -> {
                    final LoseMsg message = new LoseMsg(activePlayers.size(), verifiedPlayer);
                    player.tell(message, getSelf());
                });
            }

            getContext().unbecome();
            verifiedPlayer = null;
            verifySolution.clear();
        }

        if (activePlayers.size() != 0) {
            startNextTurn();
        }
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

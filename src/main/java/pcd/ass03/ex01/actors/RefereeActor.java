package pcd.ass03.ex01.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pcd.ass03.ex01.messages.StartGameMsg;
import pcd.ass03.ex01.messages.StartPlayerMsg;
import pcd.ass03.ex01.messages.StartTurnMsg;

import java.util.*;

/**
 * Represents the entity that coordinates actions inside the match
 */
public final class RefereeActor extends AbstractActor {
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


    public RefereeActor() {
        this.players = new HashSet<>();
        this.activePlayers = new HashSet<>();
        this.currentLap = new ArrayList<>();
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartGameMsg.class, this::handleStartGameMsg)
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
            final StartPlayerMsg message = new StartPlayerMsg(
                    startGameMsg.getCombinationSize(),
                    players,
                    getSelf()
            );
            player.tell(message, getSelf());
        });

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


    /**
     * If a message has not a known type, an error will be shown.
     * @param message the non-recognized message.
     */
    private void messageNotRecognized(final Object message) {
        log.error("Message not recognized: " + message);
    }
}

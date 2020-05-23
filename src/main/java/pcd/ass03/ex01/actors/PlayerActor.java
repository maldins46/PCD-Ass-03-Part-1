package pcd.ass03.ex01.actors;

import akka.actor.ActorRef;
import pcd.ass03.ex01.messages.*;
import pcd.ass03.ex01.utils.Combination;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// todo: check final

public final class PlayerActor extends GenericActor {

    /**
     * It memorizes all players.
     */
    private Set<ActorRef> players;

    /**
     * It memorizes the ref to the other players with the correct combination.
     */
    private final Map<ActorRef, Combination> guessedCombination;

    /**
     * It memorizes the ref to the other players with all the combinations
     * that have been tested.
     */
    private final Map<ActorRef, Set<Combination>> triedCombinations;

    /**
     * It memorizes the ref to the referee.
     */
    private ActorRef refereeRef;

    /**
     * It memorizes the combination.
     */
    private Combination myCombination;

    /**
     * It memorizes the state of the actor
     * todo: non ho capito a what a fuck serve lo stato ancora! LOL
     */
    private boolean turn;


    /**
     * costruttore
     */
    public PlayerActor() {
        this.guessedCombination = new HashMap<>();
        this.triedCombinations = new HashMap<>();
    }


    @Override
    public Receive createReceive() {
      return standardBehavior();
    }


    /**
     * This is the actor behavior used when it is not its turn.
     * @return the receive behavior.
     */
    public Receive standardBehavior() {
        return receiveBuilder()
                .match(StartPlayerMsg.class, this::handleStartGameMsg)
                .match(StartTurnMsg.class, this::handleStartTurnMsg)
                .match(GuessMsg.class, this::handleGuessMsg)
                .matchAny(this::messageNotRecognized)
                .build();
    }


    /**
     * This is the actor behavior used when it is its turn.
     * @return the receive behavior.
     */
    public Receive turnBehavior() {
        return receiveBuilder()
                .match(RespondToGuessMsg.class, this::handleRespondToGuessMsg)
                .match(TimeoutMsg.class, this::handleTimoutMsg)
                .matchAny(this::messageNotRecognized)
                .build();
    }


    /**
     * When a guessMsg arrives to the player, it will answer with
     * two numbers.
     * @param guessMsg the message
     */
    private void handleGuessMsg(final GuessMsg guessMsg) {
        final RespondToGuessMsg message = new RespondToGuessMsg(
                myCombination.computeGuessedCyphers(guessMsg.getCombination()),
                myCombination.computeGuessedPositions((guessMsg.getCombination())));

        guessMsg.getSender().tell(message, getSelf());
    }


    /**
     * When a startTurnMsg arrives to the player, this actor will enable the turn.
     * After that, it choices another player and it tries to guess his combination.
     * @param startTurnMsg the message
     */
    private void handleStartTurnMsg(final StartTurnMsg startTurnMsg) {
        getContext().become(turnBehavior(), true);
        final ActorRef playerToGuess = choicePlayer();
        final Combination combinationToGuess = choiceCombination(playerToGuess);

        final GuessMsg message = new GuessMsg(combinationToGuess, getSelf());
        playerToGuess.tell(message, getSelf());

    }


    private void handleRespondToGuessMsg(final RespondToGuessMsg respondToGuessMsg) {
        // todo
        getContext().unbecome();
    }


    private void handleTimoutMsg(final TimeoutMsg timeoutMsg) {
        // todo
        getContext().unbecome();
    }


    /**
     * This method choices a random combination to submit.
     * @param playerToGuess the actor to submit the combination.
     * @return the combination.
     */
    private Combination choiceCombination(final ActorRef playerToGuess) {
        Combination combinationToGuess = Combination.of(myCombination.getCombinationSize());
        final Set<Combination> testedCombination = triedCombinations.get(playerToGuess);

        while (testedCombination.contains(combinationToGuess)) {
            combinationToGuess = Combination.of(myCombination.getCombinationSize());
        }

        return combinationToGuess;
    }


    /**
     * This method choices a random player to guess his combination.
     * @return ActorRef to the selected actor.
     */
    private ActorRef choicePlayer() {
        ActorRef playerToGuess = players.iterator().next();

        while (!guessedCombination.containsKey(playerToGuess)) {
            playerToGuess = players.iterator().next();
        }

        return playerToGuess;
    }


    /**
     * When a startPlayerMsg arrives to the player, this actor will save the ref
     * to the other players and to the referee. It also creates the combination.
     * @param startPlayerMsg the message
     */
    private void handleStartGameMsg(final StartPlayerMsg startPlayerMsg) {
        players = startPlayerMsg.getPlayersRef();
        refereeRef = startPlayerMsg.getRefereeRef();
        myCombination = Combination.of(startPlayerMsg.getCombinationSize());
    }
}

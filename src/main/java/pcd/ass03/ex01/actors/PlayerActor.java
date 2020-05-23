package pcd.ass03.ex01.actors;

import akka.actor.ActorRef;
import pcd.ass03.ex01.messages.*;
import pcd.ass03.ex01.utils.Combination;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
    private final Map<ActorRef, List<Combination>> triedCombinations;

    /**
     * It memorizes the ref to the referee.
     */
    private ActorRef refereeRef;

    /**
     * It memorizes the combination.
     */
    private Combination myCombination;

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
    private Receive standardBehavior() {
        return receiveBuilder()
                .match(StartPlayerMsg.class, this::handleStartGameMsg)
                .match(StartTurnMsg.class, this::handleStartTurnMsg)
                .match(GuessMsg.class, this::handleGuessMsg)
                .match(VerifySolutionMsg.class, this::handleVerifySolutionMsg)
                .matchAny(this::messageNotRecognized)
                .build();
    }

    /**
     * It allows the referee to check if the solution is correct.
     * @param verifySolutionMsg the message
     */
    private void handleVerifySolutionMsg(VerifySolutionMsg verifySolutionMsg) {
        boolean solutionGuessed = false;
        if (myCombination.compare(verifySolutionMsg.getCombination())) {
            solutionGuessed = true;
        }
        final VerifySolutionResponseMsg message = new VerifySolutionResponseMsg(getSelf(), solutionGuessed);
        refereeRef.tell(message, getSelf());
    }


    /**
     * This is the actor behavior used when it is its turn.
     * @return the receive behavior.
     */
    private Receive turnBehavior() {
        return receiveBuilder()
                .match(GuessResponseMsg.class, this::handleGuessResponseMsg)
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
        final GuessResponseMsg message = new GuessResponseMsg(
                myCombination.computeGuessedCyphers(guessMsg.getCombination()),
                myCombination.computeGuessedPositions((guessMsg.getCombination())), guessMsg.getSender());

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

        triedCombinations.get(playerToGuess).add(combinationToGuess);

        final GuessMsg message = new GuessMsg(combinationToGuess, getSelf());

        playerToGuess.tell(message, getSelf());
    }

    /**
     * This method checks the answer of the Guess. It also allows the player
     * to send a SolutionMsg.
     * @param guessResponseMsg the message
     */
    private void handleGuessResponseMsg(final GuessResponseMsg guessResponseMsg) {
        final List<Combination> testedCombination = triedCombinations.get(guessResponseMsg.getSender());

        if (guessResponseMsg.getGuessedCyphers() == myCombination.getCombinationSize()) {
            guessedCombination.put(guessResponseMsg.getSender(), testedCombination.get(testedCombination.size() - 1));
        }
        if (guessedCombination.size() == players.size()) {
            final SolutionMsg message = new SolutionMsg(guessedCombination, getSelf());
            refereeRef.tell(SolutionMsg.class, getSelf());
        } else {
            final FinishTurnMsg message = new FinishTurnMsg();
            refereeRef.tell(FinishTurnMsg.class, getSelf());
        }

        getContext().unbecome();
    }

    /**
     * This method stops the player after it receives a timeout.
     * @param timeoutMsg the message
     */
    private void handleTimoutMsg(final TimeoutMsg timeoutMsg) {
        getContext().unbecome();
    }


    /**
     * This method choices a random combination to submit.
     * @param playerToGuess the actor to submit the combination.
     * @return the combination.
     */
    private Combination choiceCombination(final ActorRef playerToGuess) {
        Combination combinationToGuess = Combination.of(myCombination.getCombinationSize());
        final List<Combination> testedCombination = triedCombinations.get(playerToGuess);

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
        players.remove(getSelf());
        refereeRef = startPlayerMsg.getRefereeRef();
        myCombination = Combination.of(startPlayerMsg.getCombinationSize());
    }
}

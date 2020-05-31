package pcd.ass03.ex01.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import pcd.ass03.ex01.messages.*;
import pcd.ass03.ex01.utils.Combination;

import java.util.*;
import java.util.stream.Collectors;


public final class PlayerActor extends AbstractLoggingActor {

    /**
     * It memorizes all players. It is used a list because it is simpler
     * to handle ordination and random picking.
     */
    private List<ActorRef> players;

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
    private ActorRef referee;

    /**
     * It memorizes the combination.
     */
    private Combination combination;

    private boolean loser;


    /**
     * Default player constructor.
     */
    public PlayerActor() {
        this.players = new ArrayList<>();
        this.guessedCombination = new HashMap<>();
        this.triedCombinations = new HashMap<>();
        this.loser = false;
    }


    @Override
    public Receive createReceive() {
      return startBehavior();
    }

    private Receive startBehavior() {
        return receiveBuilder()
                .match(StartPlayerMsg.class, this::handleStartGameMsg)
                .match(StopGameMsg.class, this::handleStopGameMsg)
                .matchAny(msg -> log().error("Message not recognized: " + msg))
                .build();
    }



    /**
     * This is the actor behavior used when it is not its turn.
     * @return the receive behavior.
     */
    private Receive defaultBehavior() {
        log().info(getSelf().path().name() + " is in default behavior");
        return receiveBuilder()
                .match(StartTurnMsg.class, this::handleStartTurnMsg)
                .match(GuessMsg.class, this::handleGuessMsg)
                .match(VerifySolutionMsg.class, this::handleVerifySolutionMsg)
                .match(WinMsg.class, this::handleWinMsg)
                .match(StopGameMsg.class, this::handleStopGameMsg)
                .match(LoseMsg.class, this::handleLoseMsg)
                .matchAny(msg -> log().error("Message not recognized: " + msg))
                .build();
    }


    /**
     * This is the actor behavior used when it is its turn.
     * @return the receive behavior.
     */
    private Receive turnBehavior() {
        log().info(getSelf().path().name() + " is in turn behavior");
        return receiveBuilder()
                .match(GuessResponseMsg.class, this::handleGuessResponseMsg)
                .match(TimeoutMsg.class, this::handleTimoutMsg)
                .match(StopGameMsg.class, this::handleStopGameMsg)
                .matchAny(msg -> log().error("Message not recognized: " + msg))
                .build();
    }


    private Receive lostBehavior() {
        return receiveBuilder()
                .match(GuessMsg.class, this::handleGuessMsg)
                .match(VerifySolutionMsg.class, this::handleVerifySolutionMsg)
                .match(WinMsg.class, this::handleWinMsg)
                .match(LoseMsg.class, this::handleLoseMsg)
                .match(StopGameMsg.class, this::handleStopGameMsg)
                .matchAny(msg -> log().error("Message not recognized: " + msg))
                .build();
    }

    /**
     * When a startPlayerMsg arrives to the player, this actor will save the ref
     * to the <b>other</b> players and to the referee. It also creates the combination.
     * @param startPlayerMsg the message
     */
    private void handleStartGameMsg(final StartPlayerMsg startPlayerMsg) {
        getContext().become(defaultBehavior());

        players = startPlayerMsg.getPlayers().stream()
                .filter(player -> player != getSelf()).collect(Collectors.toList());

        players.forEach(player -> triedCombinations.put(player, new ArrayList<>()));

        referee = startPlayerMsg.getReferee();
        combination = Combination.of(startPlayerMsg.getCombinationSize());
        log().info("Started " + getSelf().path().name() + ", combination is " + combination.getCombination().toString());
    }


    /**
     * This method does not allow to player to partecipate to the game
     * after a failed solution.
     * @param loseMsg the message.
     */
    private void handleLoseMsg(final LoseMsg loseMsg) {
        if (loseMsg.getNActivePlayers() == 0) {
            getContext().stop(getSelf());
        }

        if (loseMsg.getLoser().equals(getSelf())) {
            getContext().become(lostBehavior());
            loser = true;
        }
    }


    /**
     * It stops the player when the referee sends a stopGameMsg.
     * @param stopGameMsg the message.
     */
    private void handleStopGameMsg(final StopGameMsg stopGameMsg) {
        getContext().stop(getSelf());
    }


    /**
     * It shows who is the winner.
     * @param winMsg the message.
     */
    private void handleWinMsg(final WinMsg winMsg) {
        getContext().stop(getSelf());
    }


    /**
     * It allows the referee to check if the solution is correct.
     * @param verifySolutionMsg the message.
     */
    private void handleVerifySolutionMsg(final VerifySolutionMsg verifySolutionMsg) {
        final VerifySolutionResponseMsg verSolRespMsg = new VerifySolutionResponseMsg(
                combination.compare(verifySolutionMsg.getCombination())
        );
        referee.tell(verSolRespMsg, getSelf());

    }


    /**
     * When a guessMsg arrives to the player, it will answer with
     * two numbers.
     * @param guessMsg the message
     */
    private void handleGuessMsg(final GuessMsg guessMsg) {
        log().info("Received guess message from " + getSender().path().name() + "; compare and response");
        final GuessResponseMsg message = new GuessResponseMsg(
                combination.computeGuessedCyphers(guessMsg.getCombination()),
                combination.computeGuessedPositions((guessMsg.getCombination()))
        );

        getSender().tell(message, getSelf());
    }


    /**
     * When a startTurnMsg arrives to the player, this actor will enable the turn.
     * After that, it choices another player and it tries to guess his combination.
     * @param startTurnMsg the message
     */
    private void handleStartTurnMsg(final StartTurnMsg startTurnMsg) {
        getContext().become(turnBehavior());

        final ActorRef playerToGuess = choosePlayer();
        final Combination combinationToGuess = chooseCombination(playerToGuess);

        triedCombinations.get(playerToGuess).add(combinationToGuess);

        log().info("Turn started, sent guess msg to " + playerToGuess.path().name());
        final GuessMsg guessMsg = new GuessMsg(combinationToGuess);
        playerToGuess.tell(guessMsg, getSelf());
    }


    /**
     * This method checks the answer of the Guess. It also allows the player
     * to send a SolutionMsg.
     * @param guessRespMsg the message
     */
    private void handleGuessResponseMsg(final GuessResponseMsg guessRespMsg) {
        log().info("Received guess response. Finishing turn");
        getContext().become(defaultBehavior()); // fixme

        final List<Combination> triedCombsForPlayer = triedCombinations.get(getSender());

        if (guessRespMsg.getGuessedPositions() == combination.getCombinationSize()
                && guessRespMsg.getGuessedCyphers() == 0) {
            guessedCombination.put(getSender(), triedCombsForPlayer.get(triedCombsForPlayer.size() - 1));
        }

        final Message responseToReferee;
        if (guessedCombination.size() == players.size()) {
            responseToReferee = new SolutionMsg(guessedCombination);
        } else {
            responseToReferee = new FinishTurnMsg();
        }
        referee.tell(responseToReferee, getSelf());
    }


    /**
     * This method stops the player after it receives a timeout.
     * @param timeoutMsg the message
     */
    private void handleTimoutMsg(final TimeoutMsg timeoutMsg) {
        // todo quando avremo l'utente umano, qua gli forzi lo stop del turno
        getContext().unbecome(); // fixme
    }


    /**
     * This method choices a random combination to submit, discarding
     * combinations that have been already used for that player.
     * @param playerToGuess the actor to submit the combination.
     * @return the combination.
     */
    private Combination chooseCombination(final ActorRef playerToGuess) {
        Combination combinationToGuess = Combination.of(combination.getCombinationSize());
        final List<Combination> testedCombination = triedCombinations.get(playerToGuess);

        while (testedCombination.contains(combinationToGuess)) {
            combinationToGuess = Combination.of(combination.getCombinationSize());
        }

        return combinationToGuess;
    }


    /**
     * This method choices a random player to guess his combination.
     * @return ActorRef to the selected actor.
     */
    private ActorRef choosePlayer() {
        Collections.shuffle(players);

        ActorRef playerToGuess = players.iterator().next();
        while (guessedCombination.containsKey(playerToGuess)) {
            playerToGuess = players.iterator().next();
        }

        return playerToGuess;
    }
}

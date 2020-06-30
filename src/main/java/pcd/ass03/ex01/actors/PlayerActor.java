package pcd.ass03.ex01.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pcd.ass03.ex01.messages.*;
import pcd.ass03.ex01.utils.Combination;

import java.util.*;
import java.util.stream.Collectors;


public class PlayerActor extends AbstractActor {

    /**
     * It memorizes all players. It is used a list because it is simpler
     * to handle ordination and random picking.
     */
    protected List<ActorRef> enemies;

    /**
     * It memorizes the ref to the other players with the correct combination.
     */
    private final Map<ActorRef, Combination> guessedCombination = new HashMap<>();

    /**
     * It memorizes the ref to the other players with all the combinations
     * that have been tested.
     */
    private final Map<ActorRef, List<Combination>> triedCombinations = new HashMap<>();

    /**
     * It memorizes the ref to the referee.
     */
    protected ActorRef arbiter;

    /**
     * It memorizes the combination.
     */
    private Combination combination;

    protected boolean loser;

    protected boolean isMyTurn;

    protected boolean respondsOnlyToSender;

    protected final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), getSelf().path().name());
    


    @Override
    public Receive createReceive() {
      return startBehavior();
    }

    protected Receive startBehavior() {
        return receiveBuilder()
                .match(StartPlayerMsg.class, this::handleStartGameMsg)
                .match(StopGameMsg.class, this::handleStopGameMsg)
                .matchAny(msg -> log.error("Message not recognized: " + msg))
                .build();
    }


    /**
     * This is the actor behavior used when it is not its turn.
     * @return the receive behavior.
     */
    protected Receive defaultBehavior() {
        log.debug("Default behavior");
        return receiveBuilder()
                .match(StartTurnMsg.class, this::handleStartTurnMsg)
                .match(GuessMsg.class, this::handleGuessMsg)
                .match(VerifySolutionMsg.class, this::handleVerifySolutionMsg)
                .match(WinMsg.class, this::handleWinMsg)
                .match(StopGameMsg.class, this::handleStopGameMsg)
                .match(LoseMsg.class, this::handleLoseMsg)
                .match(GuessResponseMsg.class, this::handleGuessResponseMsg)
                .matchAny(msg -> log.error("Message not recognized: " + msg))
                .build();
    }


    /**
     * This is the actor behavior used when it is its turn.
     * @return the receive behavior.
     */
    protected Receive turnBehavior() {
        log.debug("Turn behavior");
        return receiveBuilder()
                .match(GuessResponseMsg.class, this::handleGuessResponseMsg)
                .match(TimeoutMsg.class, this::handleTimeoutMsg)
                .match(StopGameMsg.class, this::handleStopGameMsg)
                .matchAny(msg -> log.error("Message not recognized: " + msg))
                .build();
    }

    protected Receive lostBehavior() {
        log.debug("Lost behavior");
        return receiveBuilder()
                .match(GuessMsg.class, this::handleGuessMsg)
                .match(VerifySolutionMsg.class, this::handleVerifySolutionMsg)
                .match(WinMsg.class, this::handleWinMsg)
                .match(LoseMsg.class, this::handleLoseMsg)
                .match(StopGameMsg.class, this::handleStopGameMsg)
                .matchAny(msg -> log.error("Message not recognized: " + msg))
                .build();
    }

    /**
     * When a startPlayerMsg arrives to the player, this actor will save the ref
     * to the <b>other</b> players and to the referee. It also creates the combination.
     * @param startPlayerMsg the message
     */
    protected void handleStartGameMsg(final StartPlayerMsg startPlayerMsg) {
        getContext().become(defaultBehavior());

        enemies = startPlayerMsg.getPlayers().stream()
                .filter(player -> player != getSelf()).collect(Collectors.toList());

        enemies.forEach(player -> triedCombinations.put(player, new ArrayList<>()));

        respondsOnlyToSender = startPlayerMsg.isResponseOnlyToSender();
        arbiter = startPlayerMsg.getReferee();
        combination = Combination.of(startPlayerMsg.getCombinationSize());
        log.info("Started, combination is " + combination.getCombination().toString());
    }


    /**
     * When a startTurnMsg arrives to the player, this actor will enable the turn.
     * After that, it choices another player and it tries to guess his combination.
     * @param startTurnMsg the message
     */
    protected void handleStartTurnMsg(final StartTurnMsg startTurnMsg) {
        getContext().become(turnBehavior());
        isMyTurn = true;

        final ActorRef playerToGuess = choosePlayer();

        if (playerToGuess == null) {
            log.error("Player null, qualcosa è andato storto in start turn!!!!");

        } else {
            final Combination combinationToGuess = chooseCombination(playerToGuess);
            final GuessMsg guessMsg = new GuessMsg(combinationToGuess);
            playerToGuess.tell(guessMsg, getSelf());
            log.debug("Turn started, sent guess msg to " + playerToGuess.path().name());
        }
    }


    /**
     * This method does not allow to player to partecipate to the game
     * after a failed solution.
     * @param loseMsg the message.
     */
    protected void handleLoseMsg(final LoseMsg loseMsg) {
        if (loseMsg.getNActivePlayers() == 0) {
            log.debug("Received loseMsg, every player have lost, aborting");
            getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());

        } else if (loseMsg.getLoser().equals(getSelf())) {
            log.info("I have lost :(");
            getContext().become(lostBehavior());
            loser = true;

        } else {
            log.debug("Someone lost, not me");
        }
    }


    /**
     * It stops the player when the referee sends a stopGameMsg.
     * @param stopGameMsg the message.
     */
    protected void handleStopGameMsg(final StopGameMsg stopGameMsg) {
        log.debug("Received stopMsg, aborting");
        getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
    }


    /**
     * It shows who is the winner.
     * @param winMsg the message.
     */
    protected void handleWinMsg(final WinMsg winMsg) {
        log.debug("Received winMsg, aborting");
        if (winMsg.getWinner().equals(getSelf())) {
           log.info(winMsg.getWinner().path().name() + " wins!");
        }

        getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
    }


    /**
     * It allows the referee to check if the solution is correct.
     * @param verifySolutionMsg the message.
     */
    private void handleVerifySolutionMsg(final VerifySolutionMsg verifySolutionMsg) {
        final VerifySolutionResponseMsg verSolRespMsg = new VerifySolutionResponseMsg(
                combination.compare(verifySolutionMsg.getCombination())
        );
        arbiter.tell(verSolRespMsg, getSelf());
        log.debug("Respond to a verify solution, result is " + combination.compare(verifySolutionMsg.getCombination()));

    }


    /**
     * When a guessMsg arrives to the player, it will answer with
     * two numbers.
     * @param guessMsg the message
     */
    private void handleGuessMsg(final GuessMsg guessMsg) {
        final GuessResponseMsg message = new GuessResponseMsg(
                combination.computeGuessedCyphers(guessMsg.getCombination()),
                combination.computeGuessedPositions((guessMsg.getCombination())),
                guessMsg.getCombination()
        );

        if (respondsOnlyToSender) {
            log.debug("Received guess message from " + getSender().path().name() + "; responding to the sender");
            getSender().tell(message, getSelf());
        } else {
            log.debug("Received guess message from " + getSender().path().name() + "; responding to all");
            enemies.forEach(player -> player.tell(message, getSelf()));
        }
    }



    /**
     * This method checks the answer of the Guess. It also allows the player
     * to send a SolutionMsg.
     * @param guessRespMsg the message
     */
    protected void handleGuessResponseMsg(final GuessResponseMsg guessRespMsg) {
        if (isMyTurn) {
            if (guessRespMsg.getGuessedPositions() == combination.getCombinationSize()
                    && guessRespMsg.getGuessedCyphers() == 0) {
                guessedCombination.put(getSender(), guessRespMsg.getCombination());
            }

            final Message responseToReferee;
            if (guessedCombination.size() == enemies.size()) {
                log.debug("Received guess response. I have the solution! Respond with SolutionMsg");
                responseToReferee = new SolutionMsg(guessedCombination);
            } else {
                log.debug("Received guess response without combination. Finishing turn");
                responseToReferee = new FinishTurnMsg();
            }
            arbiter.tell(responseToReferee, getSelf());
            isMyTurn = false;
            getContext().become(defaultBehavior());

        } else {
            log.debug("Received guess response, ignoring");
        }
    }


    /**
     * This method stops the player after it receives a timeout.
     * @param timeoutMsg the message
     */
    protected void handleTimeoutMsg(final TimeoutMsg timeoutMsg) {
        log.debug("Received timeout!");
        getContext().become(defaultBehavior());
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
        Collections.shuffle(enemies);

        for (ActorRef player : enemies) {
            if (!guessedCombination.containsKey(player))
                return player;
        }

       return null;
    }
}

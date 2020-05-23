package pcd.ass03.ex01.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pcd.ass03.ex01.messages.GuessMsg;
import pcd.ass03.ex01.messages.RespondToGuessMsg;
import pcd.ass03.ex01.messages.StartPlayerMsg;
import pcd.ass03.ex01.messages.StartTurnMsg;
import pcd.ass03.ex01.utils.Combination;

import java.util.Map;
import java.util.Set;

// todo: check final

public class PlayerActor extends AbstractActor {

    /**
     * Embedded Akka logger used to show information of the execution.
     * todo: instanziato nel costruttore?
     */
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

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
    public PlayerActor(Set<ActorRef> players, Map<ActorRef, Combination> guessedCombination, Map<ActorRef, Set<Combination>> triedCombinations, ActorRef refereeRef, Combination myCombination, boolean turn) {
        this.players = players;
        this.guessedCombination = guessedCombination;
        this.triedCombinations = triedCombinations;
        this.refereeRef = refereeRef;
        this.myCombination = myCombination;
        this.turn = turn;
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartPlayerMsg.class, this::handleStartGameMsg)
                .match(StartTurnMsg.class, this::handleStartTurnMsg)
                .match(GuessMsg.class, this::handleGuessMsg)
                .matchAny(this::messageNotRecognized)
                .build();
    }

    /**
     * When a guessMsg arrives to the player, it will answer with
     * two numbers.
     * @param guessMsg the message
     */
    private void handleGuessMsg(GuessMsg guessMsg) {


        // todo: esiste un modo per prendere il mittente di un messaggio che ti arriva ???


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

        switchTurn();
        // Enter in the state turn = true.
        final ActorRef playerToGuess = choicePlayer();

        // todo: alternativa passare dentro il messaggio la lunghezza della combinazione
        final Combination combinationToGuess = choiceCombination(playerToGuess);

        final GuessMsg message = new GuessMsg(combinationToGuess, getSelf());

        playerToGuess.tell(message, getSelf());

        // Enter in the state turn = false.
        switchTurn();
    }

    /**
     * This method choices a random combination to submit.
     * @param playerToGuess the actor to submit the combination.
     * @return the combination.
     */
    private Combination choiceCombination(ActorRef playerToGuess) {
        Combination combinationToGuess = Combination.of(myCombination.getCombinationSize());
        final Set<Combination> testedCombination = triedCombinations.get(playerToGuess);

        while(testedCombination.contains(combinationToGuess)){
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

        while(!guessedCombination.containsKey(playerToGuess)){
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

    /**
     * If a message has not a known type, an error will be shown.
     * @param message the non-recognized message.
     */
    private void messageNotRecognized(final Object message) {
        log.error("Message not recognized: " + message);
    }

    /**
     * This method allows the player to switch on/off the turn
     */
    private void switchTurn(){
        turn = !turn;
    }
}

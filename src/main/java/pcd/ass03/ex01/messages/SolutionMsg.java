package pcd.ass03.ex01.messages;

import akka.actor.ActorRef;
import pcd.ass03.ex01.utils.Combination;

import java.util.Map;

/**
 * Notifica all'arbitro di aver trovato una combinazione definitiva
 */
public final class SolutionMsg implements Message {
    private final Map<ActorRef, Combination> combinations;
    private final ActorRef sender;

    public SolutionMsg(final Map<ActorRef, Combination> combinations, final ActorRef sender) {
        this.sender = sender;
        this.combinations = combinations;
    }

    public Map<ActorRef, Combination> getCombinations() {
        return combinations;
    }

    public ActorRef getSender() {
        return sender;
    }
}

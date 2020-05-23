package pcd.ass03.ex01;

import akka.actor.ActorSystem;
import akka.actor.Props;
import pcd.ass03.ex01.actors.GuiActor;

/**
 * Class that start System.
 */
public final class Main {

    private Main() { }

    /**
     * Start system.
     * @param args, unused.
     */
    public static void main(final String[] args) {
        final ActorSystem system = ActorSystem.create("MasterMind");
        system.actorOf(Props.create(GuiActor.class), "GuiActor");
    }
}
package pcd.ass03.ex01;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class Main {
    public static void main(String[] args) throws Exception  {
        ActorSystem system = ActorSystem.create("MySystem");

        ActorRef act = system.actorOf(Props.create(HappyActor.class));
        act.tell(new HelloMsg("World"), ActorRef.noSender());
        act.tell("Another msg", ActorRef.noSender());

        Thread.sleep(1000);
    }
}
package pcd.ass03.ex01;

import akka.actor.AbstractActor;

public class HappyActor extends AbstractActor {

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(HelloMsg.class, msg -> {
			System.out.println("Hello " + msg.getContent());
		}).matchAny(m -> {
			System.out.println("Message not recognized: "+m);
		}).build();
	}
}

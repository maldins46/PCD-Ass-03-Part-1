package pcd.ass03.ex01.actors;

import pcd.ass03.ex01.messages.LogMsg;

public final class GuiActor extends GenericActor {


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(LogMsg.class, this::handleLogMsg)
                .matchAny(this::messageNotRecognized)
                .build();
    }

    private void handleLogMsg(LogMsg logMsg) {
        logMsg.getLog();
    }

}

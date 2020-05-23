package pcd.ass03.ex01.actors;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * Base class for every actor.
 * Everyone have "log" and "messageNotRecognized".
 */
public abstract class GenericActor extends AbstractActor {

    /**
     * Embedded Akka logger used to show information of the execution.
     */
    protected final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    /**
     * If a message has not a known type, an error will be shown.
     * @param message the non-recognized message.
     */
    protected final void messageNotRecognized(final Object message) {
        log.error("Message not recognized: " + message);
    }
}

package pcd.ass03.ex01.messages;

/*
 * Arbitro notifica al player che non è più il suo turno.
 */
public final class TimeoutMsg implements Message {

    private final String log;

    public TimeoutMsg (final String log){
        this.log = log;
    }

    public String getLog() {
        return log;
    }
}

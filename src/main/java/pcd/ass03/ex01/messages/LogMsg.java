package pcd.ass03.ex01.messages;

/*
 * log to notify Gui
 */
public final class LogMsg implements Message {

    /**
     * log is the string message.
     */
    private final String log;

    /**
     * Constructor.
     * @param log message to print into a Gui.
     */
    public LogMsg(final String log) {
        this.log = log;
    }

    /**
     * Getter for Log string.
     * @return log string.
     */
    public String getLog() {
        return log;
    }
}

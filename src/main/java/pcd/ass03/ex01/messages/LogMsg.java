package pcd.ass03.ex01.messages;

/*
 * log to notify Gui
 */
public final class LogMsg implements Message {

    /*
     * log is the string message
     */
    private final String log;

    public LogMsg(final String log) {
        this.log = log;
    }

    public String getLog() {
        return log;
    }
}

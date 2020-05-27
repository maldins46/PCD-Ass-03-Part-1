package pcd.ass03.ex01.utils;

import java.time.Duration;
import java.util.function.Function;

public final class TurnTimeout extends Thread {
    private static final Duration TURN_DURATION = Duration.ofSeconds(60);

    private final Function<Void, Void> timeoutCallback;

    public TurnTimeout(final Function<Void, Void> timeoutCallback) {
        this.timeoutCallback = timeoutCallback;
        this.start();
    }

    @Override
    public void run() {
        super.run();
        try {
            Thread.sleep(TURN_DURATION.toMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        timeoutCallback.apply(null);
    }
}

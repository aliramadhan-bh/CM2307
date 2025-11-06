import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoggingManager {

    private static final LoggingManager instance = new LoggingManager();

    private final Logger logger;

    private LoggingManager() {

        logger = Logger.getLogger(getClass().getName());
        logger.setUseParentHandlers(false);

        ConsoleHandler handler = new ConsoleHandler();

        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {

                return record.getLevel() + ": " + record.getMessage() + "\n";

            }
        });

        logger.addHandler(handler);
    }

    public static LoggingManager getInstance() {

        return instance;
    }

    public void logEvent(Level level, String message) {

        logger.log(level, message);
    }

    public void info(String message) {

        logEvent(Level.INFO, message);
    }

    public void error(String message) {

        logEvent(Level.SEVERE, message);
    }
}

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

class LogEntry {
    String timestamp;
    String level;
    String message;

    public LogEntry(String timestamp, String level, String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + level + " " + message;
    }
}

public class LogProcessor {

    private LinkedList<LogEntry> logQueue = new LinkedList<>();
    private LinkedList<LogEntry> errorStack = new LinkedList<>();
    private int infoCount = 0;
    private int warnCount = 0;
    private int errorCount = 0;
    private int memoryWarningCount = 0;
    private final int MAX_RECENT_ERRORS = 100;
    private LinkedList<LogEntry> recentErrors = new LinkedList<>();

    // Method to read and enqueue log entries
    public void readLogFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLogEntry(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Process each log entry and add to the queue
    private void processLogEntry(String line) {
        // Example log entry format: [2024-08-23 14:35:21] INFO User123 logged in
        // Extracting the log level and message from the line
        try {
            int firstSpaceIndex = line.indexOf(' ');
            int secondSpaceIndex = line.indexOf(' ', firstSpaceIndex + 1);
            int lastBracketIndex = line.indexOf(']');

            if (lastBracketIndex == -1 || secondSpaceIndex == -1) {
                // Invalid format, skip this line
                return;
            }

            // Extracting the parts
            String timestamp = line.substring(1, lastBracketIndex); // Removing the opening bracket
            String level = line.substring(secondSpaceIndex + 1, line.indexOf(' ', secondSpaceIndex + 1));
            String message = line.substring(line.indexOf(' ', secondSpaceIndex + 1) + 1);

            LogEntry logEntry = new LogEntry(timestamp, level, message);
            logQueue.add(logEntry);
        } catch (Exception e) {
            // Handle any other exceptions
            System.err.println("Error processing line: " + line);
        }
    }

    // Method to process and analyze the log entries in the queue
    public void analyzeLogEntries() {
        while (!logQueue.isEmpty()) {
            LogEntry logEntry = logQueue.remove(); // Dequeue entry

            // Analyze based on log level
            switch (logEntry.level) {
                case "INFO":
                    infoCount++;
                    break;
                case "WARN":
                    warnCount++;
                    if (logEntry.message.contains("Memory")) {
                        memoryWarningCount++;
                    }
                    break;
                case "ERROR":
                    errorCount++;
                    errorStack.add(logEntry); // Push ERROR logs onto the stack
                    trackRecentErrors(logEntry);
                    break;
            }
        }
    }

    // Track the last 100 error log entries
    private void trackRecentErrors(LogEntry logEntry) {
        if (recentErrors.size() == MAX_RECENT_ERRORS) {
            recentErrors.removeFirst(); // Maintain the size of recent errors list
        }
        recentErrors.add(logEntry);
    }

    // Print analysis results
    public void printAnalysis() {
        System.out.println("Log Level Counts:");
        System.out.println("INFO: " + infoCount);
        System.out.println("WARN: " + warnCount);
        System.out.println("ERROR: " + errorCount);
        System.out.println("Memory Warnings: " + memoryWarningCount);
        System.out.println("Recent 100 Errors: ");
        for (LogEntry error : recentErrors) {
            System.out.println(error);
        }
    }

    public static void main(String[] args) {
        LogProcessor logProcessor = new LogProcessor();
        String logFilePath = "src/log-data.csv"; // Adjust the file path as necessary
        logProcessor.readLogFile(logFilePath);
        logProcessor.analyzeLogEntries(); // Analyze after reading all entries
        logProcessor.printAnalysis();
    }
}

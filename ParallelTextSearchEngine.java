

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// 1. Data Model for Search Results
class SearchResult {
    private final String filePath;
    private final int lineNumber;
    private final String matchedLine;

    public SearchResult(String filePath, int lineNumber, String matchedLine) {
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.matchedLine = matchedLine;
    }

    public String getFilePath() { return filePath; }
    public int getLineNumber() { return lineNumber; }
    public String getMatchedLine() { return matchedLine; }

    @Override
    public String toString() {
        return "[" + filePath + " | Line " + lineNumber + "]: " + matchedLine.trim();
    }
}

// 2. Task Worker for Searching a Single File
class FileSearchTask implements Callable<List<SearchResult>> {
    private final File file;
    private final String keyword;

    public FileSearchTask(File file, String keyword) {
        this.file = file;
        this.keyword = keyword.toLowerCase(); // Case-insensitive search
    }

    @Override
    public List<SearchResult> call() {
        List<SearchResult> results = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains(keyword)) {
                    results.add(new SearchResult(file.getName(), lineNumber, line));
                }
                lineNumber++;
            }
        } catch (Exception e) {
            System.err.println("Error reading file " + file.getName() + ": " + e.getMessage());
        }
        return results;
    }
}

// 3. Search Engine Manager (Handles Parallel & Sequential Execution)
class ParallelSearchEngineManager {

    // Multi-threaded Search using Fixed Thread Pool
    public List<SearchResult> searchParallel(List<File> files, String keyword, int numThreads) 
            throws InterruptedException, ExecutionException {
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<List<SearchResult>>> futures = new ArrayList<>();

        for (File file : files) {
            FileSearchTask task = new FileSearchTask(file, keyword);
            futures.add(executor.submit(task));
        }

        List<SearchResult> allResults = new ArrayList<>();
        for (Future<List<SearchResult>> future : futures) {
            allResults.addAll(future.get()); // Thread-safe aggregation
        }

        executor.shutdown();
        return allResults;
    }

    // Single-threaded Search for Benchmarking
    public List<SearchResult> searchSequential(List<File> files, String keyword) {
        List<SearchResult> allResults = new ArrayList<>();
        for (File file : files) {
            FileSearchTask task = new FileSearchTask(file, keyword);
            allResults.addAll(task.call());
        }
        return allResults;
    }
}

// 4. Main Class (Public Driver Class)
public class ParallelTextSearchEngine {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==================================================");
        System.out.println("      PARALLEL TEXT SEARCH ENGINE UTILITY         ");
        System.out.println("==================================================");

        System.out.print("Enter Target Directory Path: ");
        String dirPath = scanner.nextLine();

        System.out.print("Enter Search Keyword: ");
        String keyword = scanner.nextLine();

        File folder = new File(dirPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Invalid directory path. Exiting...");
            scanner.close();
            return;
        }

        List<File> textFiles = getAllTextFiles(folder);
        System.out.println("\nFound " + textFiles.size() + " text files to scan.");

        if (textFiles.isEmpty()) {
            System.out.println("No .txt or .log files found in the directory.");
            scanner.close();
            return;
        }

        ParallelSearchEngineManager engine = new ParallelSearchEngineManager();

        // 1. Sequential Execution (1 Thread)
        System.out.println("\n--- Running Sequential Search (1 Thread) ---");
        long seqStart = System.currentTimeMillis();
        List<SearchResult> seqResults = engine.searchSequential(textFiles, keyword);
        long seqEnd = System.currentTimeMillis();
        long seqTime = seqEnd - seqStart;
        System.out.println("Sequential Search Completed in: " + seqTime + " ms");

        // 2. Parallel Execution (Multi-threaded)
        int availableCores = Runtime.getRuntime().availableProcessors();
        System.out.println("\n--- Running Parallel Search (" + availableCores + " Threads) ---");
        long parStart = System.currentTimeMillis();
        List<SearchResult> parResults = new ArrayList<>();
        try {
            parResults = engine.searchParallel(textFiles, keyword, availableCores);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long parEnd = System.currentTimeMillis();
        long parTime = parEnd - parStart;
        System.out.println("Parallel Search Completed in: " + parTime + " ms");

        // 3. Performance Metrics Output
        System.out.println("\n==================================================");
        System.out.println("               PERFORMANCE RESULTS                ");
        System.out.println("==================================================");
        System.out.println("Total Matches Found : " + parResults.size());
        System.out.println("Sequential Time     : " + seqTime + " ms");
        System.out.println("Parallel Time       : " + parTime + " ms");

        if (parTime > 0) {
            double speedup = (double) seqTime / parTime;
            System.out.printf("Speedup Achieved    : %.2fx\n", speedup);
        } else {
            System.out.println("Speedup Achieved    : Instant execution (< 1 ms)");
        }

        // Display sample matches
        System.out.println("\n--- Top Matches (First 5) ---");
        int limit = Math.min(5, parResults.size());
        for (int i = 0; i < limit; i++) {
            System.out.println(parResults.get(i));
        }

        scanner.close();
    }

    private static List<File> getAllTextFiles(File dir) {
        List<File> fileList = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    fileList.addAll(getAllTextFiles(file)); // Recursive folder scan
                } else if (file.getName().endsWith(".txt") || file.getName().endsWith(".log")) {
                    fileList.add(file);
                }
            }
        }
        return fileList;
    }
}
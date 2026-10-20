# Parallel Text Search Engine Utility

A high-performance, multi-threaded text search application written in Java. This utility scans text (`.txt`) and log (`.log`) files in a specified directory concurrently using Java's `ExecutorService` and `Callable` interfaces.

## Features
- **Multi-threaded Execution:** Utilizes dynamic Thread Pooling (`ExecutorService`) based on available CPU cores.
- **Performance Benchmarking:** Compares execution times between Sequential (1 Thread) and Parallel processing to compute speedup.
- **Thread-safe Aggregation:** Safely aggregates matching search results across concurrent threads.
- **Recursive Directory Scan:** Automatically traverses sub-folders to find all supported files.

## Project Structure
- `SearchResult`: Data model storing match details (File name, line number, content).
- `FileSearchTask`: Implements `Callable` to scan individual files concurrently.
- `ParallelSearchEngineManager`: Manages single-threaded and multi-threaded execution pools.
- `ParallelTextSearchEngine`: Main driver program for user I/O and performance benchmarking.

## Test Dataset
The utility was tested against multiple text files containing story datasets:
- `Apik Story.txt`
- `Omar Story.txt`
- Additional log/text documents in the target folder.

## How to Run
1. Clone or download the repository.
2. Open the project in any Java IDE (NetBeans, Eclipse, VS Code, IntelliJ) or compile via terminal:
   ```bash
   javac ParallelTextSearchEngine.java
   java ParallelTextSearchEngine

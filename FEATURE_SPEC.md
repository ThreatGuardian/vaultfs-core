# Advanced Data Structure Features for File System Simulator

This document outlines new features to add to the `vaultfs-core` CLI File System Simulator. Every feature proposed here maps directly to the specific data structure concepts taught: **Searching, Sorting, LinkedList, ArrayList, Graphs, Hash, Stack, String, Queue, Tree, Binary Tree**. 

By implementing these features, this project will serve as an extremely robust Data Structures and Algorithms portfolio piece.

---

## 1. Directory History & Undo (Stack & Queue)
*   **Feature:** `cd -` (go back) and command history.
*   **Concepts:** **Stack**, **Queue**, **String**
*   **Implementation:**
    *   **Stack (History/Undo):** Maintain a `Stack<DirectoryNode>` to push the user's previous working directories every time they use `cd`. Pop from the stack when they type `cd -`. Use a second `Stack<String>` to track destructive commands (`rm`, `mv`) to allow an `undo` command.
    *   **Queue (Command Buffer):** If a user pastes multiple commands (e.g., separated by `;`), push the parsed **Strings** into a `Queue<String>` and process them First-In-First-Out (FIFO).

## 2. Fast File Search & Autocomplete (Binary Tree & ArrayList)
*   **Feature:** Global file search (`find`) and tab-completion simulation.
*   **Concepts:** **Binary Tree (specifically Binary Search Tree)**, **Searching**, **ArrayList**, **String**
*   **Implementation:**
    *   **Binary Search Tree (BST):** As files and folders are created, insert their names into a BST.
    *   **Searching:** When the user types `find <filename>`, perform an $O(\log n)$ Binary Search traversal on the BST rather than an $O(n)$ search across the entire file system tree. 
    *   **ArrayList & String:** For autocomplete simulation (`autocomplete doc`), search the BST for the prefix, collect all matches into an `ArrayList<String>`, and return the list.

## 3. Directory Sorting & Listing (Sorting & ArrayList)
*   **Feature:** Ordered `ls` command (e.g., `ls -size`, `ls -name`, `ls -date`).
*   **Concepts:** **Sorting**, **ArrayList**
*   **Implementation:**
    *   When a user types `ls`, fetch the children of the current directory into an `ArrayList<FileNode>`. 
    *   Implement custom **Sorting** algorithms (e.g., Merge Sort or Quick Sort) inside a utility class. 
    *   Sort the `ArrayList` based on the requested flag (alphabetical by name, descending by file size).

## 4. Symlinks & Shortcuts (Graphs & Hash)
*   **Feature:** Creating shortcuts (`ln -s`) to other directories.
*   **Concepts:** **Graphs**, **Hash** (HashMap/HashSet), **Searching**
*   **Implementation:**
    *   **Graphs:** By adding shortcut links, your standard Tree directory structure becomes a **Directed Graph**.
    *   **Hash (Cycle Detection):** When navigating directories or calculating total folder sizes, use a `HashSet<String>` (storing absolute path strings) to track visited nodes. If you encounter a node already in the Hash, a cycle is detected, preventing infinite loops.

## 5. File Fragmentation Simulation (LinkedList & Array)
*   **Feature:** Simulating how files are stored in blocks on a disk.
*   **Concepts:** **LinkedList**, **Array** (or ArrayList)
*   **Implementation:**
    *   Simulate disk space as a massive **Array** of "Blocks".
    *   When a large file is created, it might not fit in contiguous array indices. 
    *   Use a **LinkedList** where each node represents a block index in the Array. The file's metadata points to the `head` of the LinkedList, and traversal reads the file from the scattered blocks.

## 6. Background Task Processing (Queue & String)
*   **Feature:** Simulating background downloads or large file copies.
*   **Concepts:** **Queue**, **String**
*   **Implementation:**
    *   When a user types a command like `download large_file.zip &`, push the job (represented as a **String** or Job Object) into a `Queue`.
    *   Poll the `Queue` to process tasks one by one, simulating background processing.

## 7. Fast Directory Lookups (Hash)
*   **Feature:** Instantly jumping to a deep, absolute path.
*   **Concepts:** **Hash** (HashMap), **String**
*   **Implementation:**
    *   Maintain a global `HashMap<String, DirectoryNode>` where the key is the absolute path **String** (e.g., `/root/docs/taxes`) and the value is the direct memory reference to that Node. 
    *   This provides $O(1)$ lookup for exact paths instead of traversing the Tree step-by-step.
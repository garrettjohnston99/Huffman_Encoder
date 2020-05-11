import java.util.*;
import java.io.*;
import java.awt.*;

/**
 * File compression using Huffman Encoding, a lossless compression technique that uses maps and priority queues to
 * create unique codes for each character. Characters that appear more frequently have shorter codes which are used
 * to compress/decompress.
 *
 * @author garrettjohnston Spring 2020
 */
public class HuffEncoder {

    public static void main(String[] args) throws IOException{
        String pathName = getPath();
        // Create pq of single-node trees
        PriorityQueue<BinaryTree<HuffElement>> pq = createPriorityQueue(findFrequencies(pathName));
        // Create single code tree from pq
        BinaryTree<HuffElement> huffmanTree = createHuffmanTree(pq);
        // Compress/decompress
        compress(huffmanTree, pathName);
        decompress(huffmanTree, pathName);
    }

    /**
     * Reads a file to get a map of character frequencies
     * @return Map; Key = character, Value = frequency
     */
    public static HashMap<Character, Integer> findFrequencies(String pathName) throws IOException {
        HashMap<Character, Integer> characterCounts = new HashMap<>(); // character -> count

        // Open file
        BufferedReader input = new BufferedReader(new FileReader(pathName));
        try {
            // Now read the file character by character while it still has characters in it
            for (int unicode = input.read(); unicode != -1; ) {
                Character thisChar = (char) unicode;
                // If character already exists in map, increment key
                if (characterCounts.containsKey(thisChar)) {
                    characterCounts.put(thisChar, characterCounts.get(thisChar) + 1);
                }
                // Otherwise place this character in the map
                else {
                    characterCounts.put(thisChar, 1);
                }
                // Read next character
                unicode = input.read();
            }
        } finally {
            input.close();
        }
        return characterCounts;
    }

    /**
     * Creates single-node binary trees from character map and adds them to a priority queue sorted by char frequency
     * @param characterCounts Map character -> frequency
     * @return Priority queue of BinaryTree roots, each containing a single HuffElement object
     */
    public static PriorityQueue<BinaryTree<HuffElement>> createPriorityQueue(Map<Character, Integer> characterCounts) {
        PriorityQueue<BinaryTree<HuffElement>> pq = // Create pq
                new PriorityQueue<>(Comparator.comparingInt((BinaryTree<HuffElement> tree) -> tree.getData().getFrequency()));
        for (Character key : characterCounts.keySet()) {
            // Create binary tree using new HuffElement w/ char/freq pair
            BinaryTree<HuffElement> tempTree = new BinaryTree<>(new HuffElement(key, characterCounts.get(key)));
            pq.add(tempTree);
        }
        return pq;
    }

    /**
     * Uses pq of binary trees w/ single nodes to create a tree which produces codes for each character
     * @param pq priority queue containing nodes with all character::frequency data from a text file
     * @return resulting Huffman code tree
     */
    public static BinaryTree<HuffElement> createHuffmanTree(PriorityQueue<BinaryTree<HuffElement>> pq) {
        BinaryTree<HuffElement> tree1, tree2; // Trees to combine
        while (pq.size() > 1) {     // Until we have our single Huffman Code Tree
            // Get the smallest two frequency trees
            tree1 = pq.poll(); tree2 = pq.poll();
            // Create new node data with character==null to denote inner node
            HuffElement newNode = new HuffElement(null,
                    tree1.getData().getFrequency() + tree2.getData().getFrequency());
            // Add the tree created with this new node to the priority queue
            pq.add(new BinaryTree<>(newNode, tree1, tree2));
        }
        if (!pq.isEmpty() && pq.peek().isLeaf()) {  // Handle case of single character by creating dummy node
            // Create HuffElement w/ leaf's frequency
            HuffElement dummyData = new HuffElement(null, pq.peek().getData().getFrequency());
            // Create new tree with leaf as its left child, removing leaf from queue
            BinaryTree<HuffElement> dummy = new BinaryTree<>(dummyData, pq.poll(), null);
            pq.add(dummy);  // Add new dummy node to queue(should be queue's only object)
        }
        return pq.poll();
    }

    /**
     * Assign Huffman codes to each character in a BinaryTree(nodes contain character -> frequency)
     * Pass empty map and string to helper function
     * @param huffmanTree Huffman code tree
     * @return TreeMap Character -> Huffman Code
     */
    public static Map<Character, String> getCodes(BinaryTree<HuffElement> huffmanTree) {
        HashMap<Character, String> codeMap = new HashMap<Character, String>();  // Map to pass to helper
        if (huffmanTree == null) { return codeMap; }    // Handle case of empty file; return empty
        String codeSoFar = "";
        // Pass tree, empty map, and empty code to helper function
        getCodesHelper(huffmanTree, codeMap, codeSoFar);
        return codeMap;
    }

    /**
     * Helper for getCodes
     */
    public static void getCodesHelper(BinaryTree<HuffElement> tree, Map<Character, String> codeMap, String codeSoFar) {
        if (tree.isLeaf()) { // Leaf; add code to map
            codeMap.put(tree.getData().getCharacter(), codeSoFar);
        }
        if (tree.hasRight()) { // Recurse down right, add 1 to codeSoFar
            getCodesHelper(tree.getRight(), codeMap, codeSoFar + "1");
        }
        if (tree.hasLeft()) { // Recurse down left, add 0 to codeSoFar
            getCodesHelper(tree.getLeft(), codeMap, codeSoFar + "0");
        }
    }

    /**
     * Repeatedly looks up characters in code map to write bits to compressed file
     * @param huffmanTree Huffman code tree used to create codeMap character -> code
     * @param pathName
     */
    public static void compress(BinaryTree<HuffElement> huffmanTree, String pathName) throws IOException {
        String outFile = pathName.replace(".txt", "_compressed.txt");
        Map<Character, String> codeMap = getCodes(huffmanTree); // Get codemap from tree

        BufferedReader input = new BufferedReader(new FileReader(pathName));
        BufferedBitWriter output = new BufferedBitWriter(outFile);
        try {
            // Read in characters from input file
            for (int unicode = input.read(); unicode != -1; ) {
                // Get character's code from map
                String code = codeMap.get((char) unicode);
                // Write bits for each character
                for (int i = 0; i < code.length(); i++) {
                    boolean bit = code.charAt(i) != '0';    // Assign 0/1 to false/true
                    output.writeBit(bit);
                }
                unicode = input.read();
            }
        } finally {
            input.close();
            output.close();
            System.out.println("Successfully compressed file");
        }
    }

    /**
     * Repeatedly traverses huffman tree, writing to output file until there are no more bits to decompress
     * @param huffmanTree Huffman code tree created from original file
     * @param pathName
     */
    public static void decompress(BinaryTree<HuffElement> huffmanTree, String pathName) throws IOException {
        String inFile = pathName.replace(".txt", "_compressed.txt");
        String outFile = pathName.replace(".txt", "_decompressed.txt");

        BufferedBitReader input = new BufferedBitReader(inFile);
        BufferedWriter output = new BufferedWriter(new FileWriter(outFile));
        // Create copy of huffmanTree to traverse with
        BinaryTree<HuffElement> tree = huffmanTree;
        try {
            while (input.hasNext()) {
                if (input.readBit()) {
                    tree = tree.getRight(); // 1, go right
                } else {
                    tree = tree.getLeft(); // 0, go left
                }
                if (tree.isLeaf()) { // Leaf, code for this character is complete
                    int toWrite = (int) tree.getData().getCharacter();
                    output.write(toWrite); // Write to outfile
                    // Reset tree to root node
                    tree = huffmanTree;
                }
            }
        } finally {
            input.close();
            output.close();
            System.out.println("Successfully decompressed file");
        }
    }

    /**
     * User dialog for selecting a file to compress & decompress
     * @return selected file path
     */
    public static String getPath() {
        FileDialog dialog = new FileDialog((Frame)null, "Select a file to decompress");
        dialog.setMode(FileDialog.LOAD);
        dialog.setVisible(true);
        return dialog.getDirectory() + dialog.getFile();
    }
}

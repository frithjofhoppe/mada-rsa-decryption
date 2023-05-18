package com.example.mada.huffmann;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;


public class HuffmannApplicationController {

    @FXML
    private Label welcomeText;

    private Stage stage;

    private final String HUFFMAN_TABLE_OUTER_DELIMITER = "-";
    private final String HUFFMAN_TABLE_INNER_DELIMITER = ":";
    private final String HUFFMAN_TABLE_FILE_NAME = "dec_tab.txt";
    private final String HUFFMAN_ENCODED_FILE_NAME = "output.dat";
    private final String HUFFMAN_DECODED_FILE_NAME = "decompress.txt";

    @FXML
    private void createHuffmanTableAndEncodedContent() {
        var file = selectFile("File with plain text");
        try {
            // Create dictionary from text file
            var rawContent = Files.readAllLines(file.toPath());
            var occurreneMap = calculateOccurrence(rawContent);
            var topTreeNode = createHuffmannTree(occurreneMap);
            var dictionary = createTreeDictionary(topTreeNode);

            // Convert original text to one bit string
            List<Character> rawContentChars = rawContent
                    .stream()
                    .map(line -> line.chars().mapToObj(c -> (char) c).toList())
                    .flatMap(List::stream).toList();
            String bitString = convertTextToBitString(rawContentChars, dictionary);

            // Write converted content to output.dat
            FileOutputStream encodedContentFos = new FileOutputStream(HUFFMAN_ENCODED_FILE_NAME);
            encodedContentFos.write(convertBitStringToBytePresentation(bitString));
            encodedContentFos.close();

            // Write dictionary to dec_tab.txt
            var dictionaryOutput = dictionary.keySet()
                    .stream()
                    .map(asciiSymbol -> String.format("%s%s%s", (int) asciiSymbol, HUFFMAN_TABLE_INNER_DELIMITER, dictionary.get(asciiSymbol)))
                    .collect(Collectors.joining(HUFFMAN_TABLE_OUTER_DELIMITER));
            FileWriter decTab = new FileWriter(HUFFMAN_TABLE_FILE_NAME);
            decTab.write(dictionaryOutput);
            decTab.close();
        } catch (IOException e) {
            System.out.println("Error when reading/writing file file");
        }
    }

    @FXML
    private void createDecodedContentWithHuffmanTable() {
        var fileHuffmanTable = selectFile("File with huffmann table");
        var fileEncodedContent = selectFile("File with encoded content");

        try {
            // Recover dictionary from file
            var dictionary = recoverTreeDictionaryFromFile(
                    Files.readAllLines(fileHuffmanTable.toPath()).get(0)
            );

            // Convert byte to bitString;
            StringBuilder rawContentSb = new StringBuilder();
            for (byte b : Files.readAllBytes(fileEncodedContent.toPath())) {
                var rawCodeWord = Integer.toBinaryString(Byte.toUnsignedInt(b));
                // Leading zeros are not represented
                while (rawCodeWord.length() != 8) {
                    rawCodeWord = "0".concat(rawCodeWord);
                }
                rawContentSb.append(rawCodeWord);
            }

            // Remove artificial suffix 100000....
            var rawContent = rawContentSb.toString();
            for (int i = rawContent.length() - 1; i >= 0; i--) {
                if (rawContent.charAt(i) == '1') {
                    rawContent = rawContent.substring(0, i);
                    break;
                }
            }

            // Decode bitString to symbol
            var decodedContent = decodeContent(rawContent, dictionary);

            // Write decoded content to decompress.txt
            FileWriter decodedContentFw = new FileWriter(HUFFMAN_DECODED_FILE_NAME);
            decodedContentFw.write(decodedContent);
            decodedContentFw.close();
            System.out.println(decodedContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Go through bitString and search matching sample in the dictionary
     * @param rawContent: Bit string from file
     * @param dictionary: Mapping symbol->code
     * @return Decoded content
     */
    private String decodeContent(String rawContent, Map<Character, String> dictionary) {
        // Convert bitString to symbol
        StringBuilder sb = new StringBuilder();
        // Runs as long as not all symbols are extracted from the codeWord sequence
        boolean symbolFound = false;
        while (rawContent.length() != 0) {
            for (int i = 0; i <= rawContent.length(); i++) {
                var subString = rawContent.substring(0, i);
                if (dictionary.containsValue(subString)) {
                    char symbol = getSymbolForCode(subString, dictionary);
                    sb.append(symbol);
                    // Remove found symbol from start of the rawContent
                    rawContent = rawContent.substring(i);
                    symbolFound = true;
                    break;
                }
            }
            if (!symbolFound) {
                System.out.println("File couldn't be fully encoded. Remaining content " + rawContent);
                break;
            }
            symbolFound = false;
        }
        return sb.toString();
    }

    /**
     * Get symbol from map which matched the sequence/code
     *
     * @param sequence:   Sequence for wich the code is searched
     * @param dictionary: Mapping symbol->code
     * @return Matching symbol
     */
    private char getSymbolForCode(String sequence, Map<Character, String> dictionary) {
        for (Character symbol : dictionary.keySet()) {
            var value = dictionary.get(symbol);
            if (value.equals(sequence)) {
                return symbol;
            }
        }
        throw new RuntimeException("The code " + sequence + " does not have a symbole in the dictionary");
    }

    /**
     * Restore previously saved dictionary from file string
     *
     * @param rawContent: Concatenated string list with all symbol->codeWord mappings
     * @return Dictionary containing the symbol->codeWord mapping
     */
    private Map<Character, String> recoverTreeDictionaryFromFile(String rawContent) {
        Map<Character, String> dictionary = new HashMap<>();
        for (String pair : rawContent.split(HUFFMAN_TABLE_OUTER_DELIMITER)) {
            var symbolMapping = pair.split(HUFFMAN_TABLE_INNER_DELIMITER);
            dictionary.put((char) Integer.parseInt(symbolMapping[0]), symbolMapping[1]);
        }
        return dictionary;
    }

    /**
     * Convert bitString to actual byte array
     *
     * @param bitString String like "0001010101"
     * @return Converted string->byte[]
     */
    private byte[] convertBitStringToBytePresentation(String bitString) {
        String bitString8 = fillBitStringUp(bitString);
        String[] byteChunks = bitString8.split("(?<=\\G.{" + 8 + "})");
        byte[] convertedContent = new byte[byteChunks.length];
        for (int i = 0; i < convertedContent.length; i++) {
            convertedContent[i] = (byte) Integer.parseInt(byteChunks[i], 2);
        }
        return convertedContent;
    }

    /**
     * Fill up string with 100000... so that the content can be divided by 8
     *
     * @param bitString
     * @return bitString where length % 8 == 0
     */
    private String fillBitStringUp(String bitString) {
        StringBuilder bitStringBuilder = new StringBuilder(bitString);
        bitStringBuilder.append("1");
        while (bitStringBuilder.length() % 8 != 0) {
            bitStringBuilder.append("0");
        }
        return bitStringBuilder.toString();
    }

    /**
     * Convert symbol to code by using the dictionary
     *
     * @param chars      Symbols
     * @param dictionary Mapping symbol->code
     * @return Concatenated string of codes
     */
    private String convertTextToBitString(List<Character> chars, Map<Character, String> dictionary) {
        return chars
                .stream()
                .map(dictionary::get)
                .collect(Collectors.joining());
    }

    /**
     * Create dictionary with symbol->code mapping
     *
     * @param topNode: Combined node with 100% occurrence
     * @return map symbol->code
     */
    private Map<Character, String> createTreeDictionary(TreeNode topNode) {
        final String START_CODE_WORD = "0";
        Map<Character, String> dictionary = new HashMap<>();
        internalTreeCreation(topNode, START_CODE_WORD, dictionary);
        return dictionary;
    }

    /**
     * Recursive implementation of symbol->code map creation
     *
     * @param node:       Current node in the tree
     * @param codeWord:   Concatenated code word "01001..."
     * @param dictionary: Symbol->code mapping
     */
    private void internalTreeCreation(TreeNode node, String codeWord, Map<Character, String> dictionary) {
        if (node == null) {
            return;
        }
        if (node.isALeaf()) {
            dictionary.put(node.getSymbolAsChar(), codeWord);
        }
        internalTreeCreation(node.getLeft(), codeWord + "0", dictionary);
        internalTreeCreation(node.getRight(), codeWord + "1", dictionary);
    }

    /**
     * Convert occurrence in a TreeNode structure
     *
     * @param symbolOccurrence: Occurrence of a symbol as percentage e,g, 40.0 -> 40%
     * @return top node with the highest occurrence of the tree with reference to the nodes below
     */
    private TreeNode createHuffmannTree(Map<Character, Double> symbolOccurrence) {
        // Add all elements to a priority queue, sorted by the occurrence
        PriorityQueue<TreeNode> heap = new PriorityQueue<>();
        symbolOccurrence.forEach((asciiSymbol, occurrence) -> heap.add(new TreeNode(String.valueOf(asciiSymbol), occurrence)));

        while (heap.size() > 1) {
            var left = heap.poll();
            var right = heap.poll();
            // Merge nodes with the smallest occurrence into new node
            heap.add(TreeNode.mergeNode(left, right));
        }
        // Return head of tree
        return heap.peek();
    }

    /**
     * Calculates the occurrence of ascii symbole in a text
     *
     * @param rawContent Lines of a file
     * @return Map<asciiSymbol, occurrence percentage>
     */
    private Map<Character, Double> calculateOccurrence(List<String> rawContent) {
        double totalCount = 0;
        HashMap<Character, Integer> occurrenceCount = new HashMap<>();
        for (String line : rawContent) {
            for (char asciiSymbol : line.toCharArray()) {
                totalCount++;
                var entry = occurrenceCount.get(asciiSymbol);
                if (entry == null) {
                    occurrenceCount.put(asciiSymbol, 1);
                } else {
                    occurrenceCount.put(asciiSymbol, ++entry);
                }
            }
        }

        HashMap<Character, Double> occurrencePercentage = new HashMap<>();
        double finalTotalCount = totalCount;
        occurrenceCount.forEach(
                (asciiSymbol, count) -> occurrencePercentage.put(asciiSymbol, (count * 100) / finalTotalCount)
        );
        return occurrencePercentage;
    }

    /**
     * Pick file from path
     *
     * @param title
     * @return
     */
    private File selectFile(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        return fileChooser.showOpenDialog(stage);
    }
}

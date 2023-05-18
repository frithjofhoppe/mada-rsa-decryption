package com.example.mada_rsa_project_2;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;


public class HuffmannApplicationController {

    @FXML
    private Label welcomeText;

    private Stage stage;


    @FXML
    private void createDecodedContentWithHuffmanTable() {

    }

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
                    .map(line -> line.chars().mapToObj(c -> (char)c).toList())
                    .flatMap(List::stream).toList();
            String bitString = convertTextToBitString(rawContentChars, dictionary);

            // Write converted content to output.dat
            FileOutputStream encodedContentFos = new FileOutputStream("output.dat");
            encodedContentFos.write(convertBitStringToBytePresentation(bitString));
            encodedContentFos.close();

            // Write dictionary to dec_tab.txt
            var dictionaryOutput = dictionary.keySet()
                    .stream()
                    .map(asciiSymbol -> String.format("%s:%s", (int)asciiSymbol, dictionary.get(asciiSymbol)))
                    .collect(Collectors.joining("-"));
            FileWriter decTab = new FileWriter("dec_tab.txt");
            decTab.write(dictionaryOutput);
            decTab.close();
        } catch (IOException e) {
            System.out.println("Error when reading/writing file file");
        }
    }

    /**
     * Convert bitString to actual byte array
     * @param bitString String like "0001010101"
     * @return Converted string->byte[]
     */
    private byte[] convertBitStringToBytePresentation(String bitString) {
        String bitString8 = fillBitStringUp(bitString);
        String[] byteChunks = bitString8.split("(?<=\\G.{" + 8 + "})");
        byte[] convertedContent = new byte[byteChunks.length];
        for(int i = 0; i < convertedContent.length; i++){
            convertedContent[0] = (byte)Integer.parseInt(byteChunks[0],2);
        }
        return convertedContent;
    }

    /**
     * Fill up string with 100000... so that the content can be divided by 8
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
     * @param chars Symbols
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
     * @param node: Current node in the tree
     * @param codeWord: Concatenated code word "01001..."
     * @param dictionary: Symbol->code mapping
     */
    private void internalTreeCreation(TreeNode node, String codeWord, Map<Character, String> dictionary) {
        if(node == null){
            return;
        }
        if(node.isALeaf()) {
            dictionary.put(node.getSymbolAsChar(), codeWord);
        }
        internalTreeCreation(node.getLeft(), codeWord + "0", dictionary);
        internalTreeCreation(node.getRight(), codeWord + "1", dictionary);
    }

    /**
     * Convert occurrence in a TreeNode structure
     * @param symbolOccurrence: Occurrence of a symbol as percentage e,g, 40.0 -> 40%
     * @return top node with the highest occurrence of the tree with reference to the nodes below
     */
    private TreeNode createHuffmannTree(Map<Character, Double> symbolOccurrence){
        // Add all elements to a priority queue, sorted by the occurrence
        PriorityQueue<TreeNode> heap = new PriorityQueue<>();
        symbolOccurrence.forEach((asciiSymbol, occurrence) -> heap.add(new TreeNode(String.valueOf(asciiSymbol), occurrence)));

        while (heap.size() > 1) {
            var left = heap.poll();
            var right = heap.poll();
            // Merge nodes with the smallest occurrence into new node
            heap.add(TreeNode.mergeNode(left,right));
        }
        // Return head of tree
        return heap.peek();
    }

    /**
     * Calculates the occurrence of ascii symbole in a text
     * @param rawContent Lines of a file
     * @return Map<asciiSymbol, occurrence percentage>
     */
    private Map<Character,Double> calculateOccurrence(List<String> rawContent) {
        double totalCount = 0;
        HashMap<Character, Integer> occurrenceCount = new HashMap<>();
        for(String line : rawContent) {
            for(char asciiSymbol : line.toCharArray()) {
                totalCount++;
                var entry = occurrenceCount.get(asciiSymbol);
                if(entry == null) {
                    occurrenceCount.put(asciiSymbol, 1);
                } else {
                    occurrenceCount.put(asciiSymbol,++entry);
                }
            }
        }

        HashMap<Character, Double> occurrencePercentage = new HashMap<>();
        double finalTotalCount = totalCount;
        occurrenceCount.forEach(
                (asciiSymbol, count) -> occurrencePercentage.put(asciiSymbol, (count * 100)/ finalTotalCount)
        );
        return occurrencePercentage;
    }

    /**
     * Encrypt plain text file with existing public key
     */
    @FXML
    public void encryptFile() {
        try {
            // Exercise 2
            // a) Select public key file
            File publicKeyFile = selectFile("Public Key");
            List<String> publicKeyPair = getKeyPairOfFile(publicKeyFile);
            BigInteger n = new BigInteger(publicKeyPair.get(0));
            BigInteger a = new BigInteger(publicKeyPair.get(1));
            File plainTextFile = selectFile("Chose text file with content");
            BufferedReader plainTextFileReader = new BufferedReader(new FileReader(plainTextFile));

            // b) Convert characters to ASCII and apply fast exponentiation
            int x = 0;
            ArrayList<String> processed = new ArrayList<>();
            while ((x = plainTextFileReader.read()) != -1) {
                System.out.println(x);
                processed.add(
                        fastMod(new BigInteger(String.valueOf(x)), a, n).toString()
                );
            }

            // c) Save encrypted file
            FileWriter writer = new FileWriter("cipher.txt");
            writer.write(processed.stream().collect(Collectors.joining(",")));
            writer.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Decrypt ciphered content with existing private key
     */
    @FXML
    public void decryptFile() {
        try {
            // Exercise 3
            // a) Select private key file
            File publicKeyFile = selectFile("Private Key");
            List<String> publicKeyPair = getKeyPairOfFile(publicKeyFile);
            BigInteger n = new BigInteger(publicKeyPair.get(0));
            BigInteger d = new BigInteger(publicKeyPair.get(1));

            File plainTextFile = selectFile("Chose text file with content");
            List<String> input = Arrays.stream(String.join("", Files.readAllLines(plainTextFile.toPath())).split(",")).toList();
            List<String> processed = new ArrayList<>();

            for (String x : input) {
                processed.add(
                        Character.toString(
                                (char)Integer.parseInt(
                                        fastMod(new BigInteger(String.valueOf(x)), d, n).toString())
                        )
                );
            }

            // Store decrypted file
            FileWriter writer = new FileWriter("text-d.txt");
            writer.write(String.join("", processed));
            writer.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate RSA key pair
     */
    @FXML
    private void generateRSAKeyPair() {
        try {
            // Exercise 1
            // a) Generate random prime numbers
            BigInteger p = generateRandomPrime();
            BigInteger q = generateRandomPrime();
            BigInteger n = p.multiply(q);
            BigInteger phiOfN = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

            // b)  Choose public exponent e and calculate private exponent d
            BigInteger e = generateCoprime(phiOfN);
            BigInteger[] values = extendedEuclideanAlgorithm(phiOfN, e);
            BigInteger d = values[2];

            System.out.println("p:" + p);
            System.out.println("q:" + q);
            System.out.println("n:" + n);
            System.out.println("PhiOfN:" + phiOfN);
            System.out.println("e:" + e);
            System.out.println("d:" + d);
            String publicKeyPair = "(" + n + "," + e + ")";
            String privateKeyPair = "(" + n + "," + d + ")";

            // c) Write public/private key pairs to file
            FileWriter privateKeyFw = new FileWriter("sk.txt");
            FileWriter publicKeyFw = new FileWriter("pk.txt");
            privateKeyFw.write(privateKeyPair);
            publicKeyFw.write(publicKeyPair);
            privateKeyFw.close();
            publicKeyFw.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException exception) {
            System.out.println("An error occurred.");
            exception.printStackTrace();
        }
    }

    /**
     * Calculating mod by using fast exponentiation
     * @param basis
     * @param exponent
     * @param modOperand
     * @return: Rest of basis^exponent mod modOperand
     */
    private BigInteger fastMod(BigInteger basis, BigInteger exponent, BigInteger modOperand) {
        String binary = exponent.toString(2);
        BigInteger h = new BigInteger("1");
        BigInteger k = basis;

        for (int i = binary.length() - 1; i >= 0; i--) {
            char binaryValue = binary.charAt(i);
            if (binaryValue == '1') {
                h = (h.multiply(k)).mod(modOperand);
            }
            k = k.pow(2).mod(modOperand);
        }

        return h;
    }

    private List<String> getKeyPairOfFile(File file) throws IOException {
        return Arrays.stream(String.join("", Files.readAllLines(file.toPath()))
                        .split("[(),]"))
                .filter(s -> !s.isBlank())
                .toList();
    }

    /**
     * Pick file
     * @param title
     * @return
     */
    private File selectFile(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        return fileChooser.showOpenDialog(stage);
    }

    /**
     * Generate public exponent starting by three
     * @param n
     * @return
     */
    private BigInteger generateCoprime(BigInteger n) {
        BigInteger result = new BigInteger("3");
        while (result.compareTo(n) >= 0 || !result.gcd(n).equals(BigInteger.ONE)) {
            result = result.add(new BigInteger("1"));
        }
        return result;
    }

    /**
     * Generate random 8 digit prime number
     * @return
     */
    private BigInteger generateRandomPrime() {
        Random random = new Random();
        int maxDigits = 8;
        BigInteger prim = new BigInteger(maxDigits * 3, random);
        while (!prim.isProbablePrime(100) || prim.toString().length() > maxDigits) {
            prim = new BigInteger(maxDigits * 3, random);
        }
        return prim;
    }

    /**
     * Calculate a', x0, y0
     * @param a
     * @param b
     * @return [a', x0, y0]
     */
    private BigInteger[] extendedEuclideanAlgorithm(BigInteger a, BigInteger b) {
        var result = internalEuclidAlgo(a,b);
        if(result[2].compareTo(BigInteger.ZERO) < 0) {
            result[2] = result[2].add(a);
        }
        return result;
    }

    private BigInteger[] internalEuclidAlgo(BigInteger a, BigInteger b) {
        if(b.equals(BigInteger.ZERO)) {
            return new BigInteger[] {
                    a, BigInteger.ONE, BigInteger.ZERO
            };
        }
        var modified = internalEuclidAlgo(b, a.mod(b));
        return new BigInteger[] {
                modified[0],
                modified[2],
                modified[1].subtract(
                        a.divideAndRemainder(b)[0].multiply(modified[2])
                )
        };
    }
}

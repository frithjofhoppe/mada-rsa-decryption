package com.example.mada_rsa_project_2;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class RSAApplicationController {

    @FXML
    private Label welcomeText;

    private Stage stage;

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

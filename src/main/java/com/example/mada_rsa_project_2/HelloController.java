package com.example.mada_rsa_project_2;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class HelloController {

    @FXML
    private Label welcomeText;

    private Stage stage;

    @FXML
    public void encryptFile() {
        try {
            // Import existing key pair
            File publicKeyFile = selectFile("Public Key");
            List<String> publicKeyPair = getKeyPairOfFile(publicKeyFile);
            BigInteger n = new BigInteger(publicKeyPair.get(0));
            BigInteger a = new BigInteger(publicKeyPair.get(1));

            File plainTextFile = selectFile("Chose text file with content");
            BufferedReader plainTextFileReader = new BufferedReader(new FileReader(plainTextFile));

            int x = 0;
            ArrayList<String> processed = new ArrayList<>();
            while ((x = plainTextFileReader.read()) != -1) {
                System.out.println(x);
                processed.add(
                        fastMod(new BigInteger(String.valueOf(x)), a, n).toString()
                );
            }

            FileWriter writer = new FileWriter("cipher.txt");
            writer.write(processed.stream().collect(Collectors.joining(",")));
            writer.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void decryptFile() {
        try {
            // Import existing key pair
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

            FileWriter writer = new FileWriter("plain.txt");
            writer.write(String.join("", processed));
            writer.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    protected void calculateFile(String fileType, String outputFileName, String outputDelimiter, IKeyPair processor) {
        try {
            // Import existing key pair
            File publicKeyFile = selectFile(fileType);
            List<String> publicKeyPair = getKeyPairOfFile(publicKeyFile);
            BigInteger n = new BigInteger(publicKeyPair.get(0));
            BigInteger a = new BigInteger(publicKeyPair.get(1));

            File plainTextFile = selectFile("Chose text file with content");
            BufferedReader plainTextFileReader = new BufferedReader(new FileReader(plainTextFile));

            int x = 0;
            ArrayList<String> processed = processor.process(a, n);

            FileWriter writer = new FileWriter(outputFileName);
            writer.write(processed.stream().collect(Collectors.joining(outputDelimiter)));
            writer.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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

    private File selectFile(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        return fileChooser.showOpenDialog(stage);
    }

    @FXML
    private void generateRSAKeyPair() {
        try {
//            BigInteger p = generateRandomPrime();
//            BigInteger q = generateRandomPrime();
//            BigInteger n = p.multiply(q);
//            BigInteger phiOfN = phi(n);
//            BigInteger e = generateCoprime(phiOfN);
//            BigInteger[] values = extendedEuclideanAlgorithm(n, e);
//            BigInteger d = values[2];
//            System.out.println("p:" + p);
//            System.out.println("q:" + q);
//            System.out.println("n:" + n);
//            System.out.println("PhiOfN:" + phiOfN);
//            System.out.println("e:" + e);
//            System.out.println("d:" + d);

            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();
            RSAPrivateKey privateKey = (RSAPrivateKey) pair.getPrivate();
            RSAPublicKey publicKey = (RSAPublicKey) pair.getPublic();
            BigInteger n = privateKey.getModulus();
            BigInteger e = publicKey.getPublicExponent();
            BigInteger d = privateKey.getPrivateExponent();

            String publicKeyPair = "(" + n + "," + e + ")";
            String privateKeyPair = "(" + n + "," + d + ")";

            FileWriter writer1 = new FileWriter("sk.txt");
            FileWriter writer2 = new FileWriter("pk.txt");
            writer1.write(privateKeyPair);
            writer2.write(publicKeyPair);
            writer1.close();
            writer2.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException exception) {
            System.out.println("An error occurred.");
            exception.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // Generiert teilerfremde Zahl zu n
    public static BigInteger generateCoprime(BigInteger n) {
        BigInteger result = new BigInteger("3");
        while (result.compareTo(n) >= 0 || !result.gcd(n).equals(BigInteger.ONE)) {
            result = result.add(new BigInteger("1"));
        }
        return result;
    }

    // Primzahlen generieren
    public static BigInteger generateRandomPrime() {
        Random random = new Random();
        int maxDigits = 8;
        BigInteger prim = new BigInteger(maxDigits * 3, random);
        while (!prim.isProbablePrime(100) || prim.toString().length() > maxDigits) {
            prim = new BigInteger(maxDigits * 3, random);
        }
        return prim;
    }

    // Eulische Phi Funktion
    public static BigInteger phi(BigInteger n) {
        BigInteger result = n;
        BigInteger i = BigInteger.valueOf(2);
        while (i.multiply(i).compareTo(n) <= 0) {
            if (n.mod(i).equals(BigInteger.ZERO)) {
                result = result.subtract(result.divide(i));
                while (n.mod(i).equals(BigInteger.ZERO)) {
                    n = n.divide(i);
                }
            }
            i = i.add(BigInteger.ONE);
        }
        if (n.compareTo(BigInteger.ONE) > 0) {
            result = result.subtract(result.divide(n));
        }
        return result;
    }

    // Erweiterter euklidischer Algorithmus
    public static BigInteger[] extendedEuclideanAlgorithm(BigInteger a, BigInteger b) {
        BigInteger[] result = new BigInteger[3];
        if (b.compareTo(BigInteger.ZERO) == 0) {
            result[0] = a;
            result[1] = BigInteger.ONE;
            result[2] = BigInteger.ZERO;
            return result;
        }
        BigInteger[] temp = extendedEuclideanAlgorithm(b, a.mod(b));
        result[0] = temp[0];
        result[1] = temp[2];
        result[2] = temp[1].subtract(a.divide(b).multiply(temp[2]));
        return result;
    }
}

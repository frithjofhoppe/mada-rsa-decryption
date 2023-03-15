package com.example.mada_rsa_project_2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class HelloController {

    @FXML
    private Label welcomeText;

    private Stage stage;

    @FXML
    protected void uploadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Text File to encrypt");
        fileChooser.showOpenDialog(stage);
    }
    @FXML
    private void generateRSAKeyPair(){
        BigInteger p = generateRandomPrime();
        BigInteger q = generateRandomPrime();
        BigInteger n = p.multiply(q);
        BigInteger phiOfN = phi(n);
        BigInteger e = generateCoprime(phiOfN);
        BigInteger[] values = extendedEuclideanAlgorithm(n, e);
        BigInteger d = values[2];

        System.out.println("p:" + p);
        System.out.println("q:" + q);
        System.out.println("n:" + n);
        System.out.println("PhiOfN:" + phiOfN);
        System.out.println("e:" + e);
        System.out.println("d:" + d);


        String publicKey = "("+n+","+e+")";
        String privateKey = "("+n+","+d+")";

        new File(System.getProperty("user.home") + "/Downloads/", "sk.txt");
        new File(System.getProperty("user.home") + "/Downloads/", "pk.txt");

        try {

            FileWriter writer1 = new FileWriter("sk.txt");
            FileWriter writer2 = new FileWriter("pk.txt");
            writer1.write(privateKey);
            writer2.write(publicKey);
            writer1.close();
            writer2.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException exception) {
            System.out.println("An error occurred.");
            exception.printStackTrace();
        }
    }

    // Generiert teilerfremde Zahl zu n
    public static BigInteger generateCoprime(BigInteger n) {
        BigInteger result;
        do {
            result = new BigInteger(n.bitLength(), new Random());
        } while (result.compareTo(n) >= 0 || !result.gcd(n).equals(BigInteger.ONE));
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

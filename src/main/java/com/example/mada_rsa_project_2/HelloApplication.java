package com.example.mada_rsa_project_2;

import java.math.BigInteger;
import java.util.Random;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }


    private String generateRSAKeyPair(){
        BigInteger p = generateRandomPrime();
        BigInteger q = generateRandomPrime();
        BigInteger n = p.multiply(q);
        BigInteger phiOfN = phi(n);
        BigInteger e = generateCoprime(phiOfN);

        extendedEuclideanAlgorithm(phiOfN, );
        return "";
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
        Random rnd = new Random();
        BigInteger prime;
        do {
            prime = new BigInteger(512, rnd);
        } while(!prime.isProbablePrime(100));
        return prime;
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

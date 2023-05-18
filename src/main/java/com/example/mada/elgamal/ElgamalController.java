package com.example.mada.elgamal;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.math.BigInteger;
import java.util.Random;

public class ElgamalController {

    private final BigInteger primNumber = new BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA051015728E5A8AACAA68FFFFFFFFFFFFFFFF", 16);
    private final BigInteger erzeuger = BigInteger.valueOf(2);
    private Key privateKey;
    private Key publicKey;
    @FXML
    private Label welcomeText;
    @FXML
    public void decryptFile(){

    };
    @FXML
    public void encryptFile(){

    };
    private Stage stage;

    @FXML
    private void generateElgamal() {
        BigInteger b =  generateRandomNumberLessThan(primNumber);

        // generate public and private key
        privateKey = new Key(primNumber, erzeuger, b);
        publicKey= new Key(primNumber, erzeuger, erzeuger.modPow(b, primNumber));

        // Message to be encrypted
        BigInteger message = new BigInteger("700");

        // Encryption
        Encryption encryption = encrypt(message);

        System.out.println("Encrypted Message: " + encryption.getCipherText());
        System.out.println("Random Number k: " + encryption.getKey());

        // Decryption
        BigInteger decryptedMessage = decrypt(encryption);
        System.out.println("Decrypted Message: " + decryptedMessage);
    }

    private BigInteger generateRandomNumberLessThan(BigInteger limit) {
        Random random = new Random();
        BigInteger range = limit.subtract(BigInteger.valueOf(1)).add(BigInteger.ONE);
        BigInteger randomNumber = new BigInteger(range.bitLength(), random);
        while (randomNumber.compareTo(range) >= 0) {
            randomNumber = new BigInteger(range.bitLength(), random);
        }
        return randomNumber.add(BigInteger.valueOf(1));
    }

    private Encryption encrypt(BigInteger message) {
        BigInteger randomNumberA = generateRandomNumberLessThan(publicKey.getPrimeNumber());

        BigInteger erzeugerHochA = publicKey.getErzeuger().modPow(randomNumberA, publicKey.getPrimeNumber());

        BigInteger erzeugerHochBA = message.multiply(publicKey.getB().modPow(randomNumberA, publicKey.getPrimeNumber())).mod(publicKey.getPrimeNumber());

        return new Encryption(erzeugerHochA, erzeugerHochBA);
    }

    private BigInteger decrypt(Encryption encryption) {
        BigInteger secretKey = encryption.getKey().modPow(privateKey.getB(), privateKey.getPrimeNumber());

        BigInteger modInverse = secretKey.modInverse(privateKey.getPrimeNumber());

        return encryption.getCipherText().multiply(modInverse).mod(privateKey.getPrimeNumber());
    }
}
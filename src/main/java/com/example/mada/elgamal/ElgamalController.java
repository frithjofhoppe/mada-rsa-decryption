package com.example.mada.elgamal;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ElgamalController {

    private final BigInteger primNumber = new BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA051015728E5A8AACAA68FFFFFFFFFFFFFFFF", 16);
    private final BigInteger erzeuger = BigInteger.valueOf(2);
    private Stage stage;
    @FXML
    private Label welcomeText;
    @FXML
    public void decryptFile(){
        // Check whether a private key is present at the file path
        var privateKeyFile = new File("sk.txt");
        if(!privateKeyFile.isFile()){
            throw new RuntimeException("Public key doesn't exists in file path");
        }

        try {
            var privateKey = new Key(
                    primNumber,
                    erzeuger,
                    new BigInteger(Files.readAllLines(privateKeyFile.toPath()).get(0))
            );
            var rawFileContent = Files.readAllLines(
                    selectFile("Select file with encrypted content (encrypted file)").toPath()
            );
            var decryptedContent = decryptFile(privateKey, rawFileContent);
            FileWriter decryptedContentFw = new FileWriter("text-d.txt");
            decryptedContentFw.write(decryptedContent);
            decryptedContentFw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String decryptFile(Key privatKey, List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            for (String element : line.split(";")) {
                var tuple = element.substring(1, element.length()-1).split(",");
                var decrypted = decrypt(
                        privatKey,
                        new Encryption(
                                new BigInteger(tuple[0]),
                                new BigInteger(tuple[1])
                        )
                );
                // Convert decrypted number back to ascii char
                sb.append((char)decrypted.intValue());
            }
        }
        return sb.toString();
    }

    @FXML
    public void encryptFile(){

        // Check whether a public key is present at the file path
        var publicKeyFile = new File("pk.txt");
        if(!publicKeyFile.isFile()){
            throw new RuntimeException("Public key doesn't exists in file path");
        }

        try {
            // Recover public key
            var publicKey = new Key(
                    primNumber,
                    erzeuger,
                    new BigInteger(Files.readAllLines(publicKeyFile.toPath()).get(0))
            );
            var rawFileContent = Files.readAllLines(
                    selectFile("Select file with raw content (original file)").toPath()
            );

            // Encrypt each ascii symbol from file and save it as chiffre.txt
            var encryptedContent = encryptFile(publicKey, rawFileContent);
            FileWriter encryptedContentFw = new FileWriter("chiffre.txt");
            encryptedContentFw.write(encryptedContent);
            encryptedContentFw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

    private String encryptFile(Key publicKey, List<String> lines) {
        List<String> pairs = new ArrayList<>();
        for (String line : lines) {
            for (char symbol : line.toCharArray()) {
                // User ascii representation of letter for encryption
                var encryption = encrypt(publicKey, BigInteger.valueOf((int)symbol));
                pairs.add(
                        String.format(
                                "(%s,%s)",
                                encryption.getKey(),
                                encryption.getCipherText()
                        )
                );
            }
        }
        return String.join(";", pairs);
    }

    @FXML
    private void generateElgamal() {
        BigInteger b =  generateRandomNumberLessThan(primNumber);

        // generate public and private key
        Key privateKey = new Key(primNumber, erzeuger, b);
        Key publicKey= new Key(primNumber, erzeuger, erzeuger.modPow(b, primNumber));

        // Message to be encrypted
        BigInteger message = new BigInteger("700");

        // Encryption
        Encryption encryption = encrypt(publicKey, message);

        System.out.println("Encrypted Message: " + encryption.getCipherText());
        System.out.println("Random Number k: " + encryption.getKey());

        // Decryption
        BigInteger decryptedMessage = decrypt(privateKey, encryption);
        System.out.println("Decrypted Message: " + decryptedMessage);

        try {
            // Save public key in pk.txt
            FileWriter publicKeyFw = new FileWriter("pk.txt");
            publicKeyFw.write(publicKey.getB().toString());
            publicKeyFw.close();

            FileWriter privateKeyFw = new FileWriter("sk.txt");
            privateKeyFw.write(privateKey.getB().toString());
            privateKeyFw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Pick file from path (für Alain :-) )
     *
     * @param title
     * @return
     */
    private File selectFile(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        return fileChooser.showOpenDialog(stage);
    }

    /**
     * Generate random number for assumed *phi of n group
     * @param limit
     * @return
     */
    private BigInteger generateRandomNumberLessThan(BigInteger limit) {
        Random random = new Random();
        // #FIXME subtract(1) + add(1) not really makes sense
        BigInteger range = limit.subtract(BigInteger.ONE);
        BigInteger randomNumber = new BigInteger(range.bitLength(), random);
        while (randomNumber.compareTo(range) >= 0) {
            randomNumber = new BigInteger(range.bitLength(), random);
        }
        return randomNumber.add(BigInteger.valueOf(1));
    }

    private Encryption encrypt(Key key, BigInteger message) {
        BigInteger randomNumberA = generateRandomNumberLessThan(key.getPrimeNumber());

        BigInteger erzeugerHochA = key.getErzeuger().modPow(randomNumberA, key.getPrimeNumber());

        BigInteger erzeugerHochBA = message.multiply(key.getB().modPow(randomNumberA, key.getPrimeNumber())).mod(key.getPrimeNumber());

        return new Encryption(erzeugerHochA, erzeugerHochBA);
    }

    private BigInteger decrypt(Key key, Encryption encryption) {
        BigInteger secretKey = encryption.getKey().modPow(key.getB(), key.getPrimeNumber());

        BigInteger modInverse = secretKey.modInverse(key.getPrimeNumber());

        return encryption.getCipherText().multiply(modInverse).mod(key.getPrimeNumber());
    }
}
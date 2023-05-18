package com.example.mada.elgamal;

import java.math.BigInteger;

public class Encryption {
    private BigInteger key;
    private BigInteger cipherText;

    public Encryption(BigInteger key, BigInteger cipherText) {
        this.key = key;
        this.cipherText = cipherText;
    }
    public BigInteger getKey() {
        return key;
    }
    public BigInteger getCipherText() {
        return cipherText;
    }
}

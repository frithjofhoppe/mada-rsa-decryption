package com.example.mada.elgamal;

import java.math.BigInteger;

public class Key {
    private final BigInteger primeNumber;
    private final BigInteger erzeuger;
    private final BigInteger b;
    public Key(BigInteger primeNumber, BigInteger erzeuger, BigInteger b) {
        this.primeNumber = primeNumber;
        this.erzeuger = erzeuger;
        this.b = b;
    }
    public BigInteger getPrimeNumber() {
        return primeNumber;
    }

    public BigInteger getErzeuger() {
        return erzeuger;
    }

    public BigInteger getB() {
        return b;
    }

}

package com.example.mada_rsa_project_2;

import java.math.BigInteger;
import java.util.ArrayList;

@FunctionalInterface
public interface IKeyPair {
    public ArrayList<String> process(BigInteger a, BigInteger n);
}

package com.gpb.datafirewall.ignite.jwt.exception;

public class JwtInvalidSecretException extends RuntimeException {

    public JwtInvalidSecretException() {
        super("Wrong secret");
    }
}
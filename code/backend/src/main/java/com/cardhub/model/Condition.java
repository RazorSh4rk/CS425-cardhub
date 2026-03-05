package com.cardhub.model;

public enum Condition {
    MINT, NEAR_MINT, PLAYED, DAMAGED;

    public double priceMultiplier() {
        return switch (this) {
            case MINT -> 1.0;
            case NEAR_MINT -> 0.9;
            case PLAYED -> 0.7;
            case DAMAGED -> 0.4;
        };
    }
}

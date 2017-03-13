package com.github.walterfan.checklist.domain;

/**
 * Created by walter on 12/03/2017.
 */
public enum TokenType {
    GENERAL(0),
    ACCOUNT_ACTIVATE(1),
    PASSWORD_RESET(2);

    private final int value;

    TokenType(int value) {
        this.value = value;
    }

    public int compare(TokenType other) {
        if (this.value > other.value) {
            return 1;
        } else if (this.value == other.value) {
            return 0;
        } else {
            return -1;
        }
    }
}

package com.github.walterfan.msa.common.domain;

/**
 * Created by walter on 12/03/2017.
 */
public enum UserStatus {
    pending(0),
    inactive(1),
    active(2);

    private final int value;

    UserStatus(int value) {
        this.value = value;
    }

    public int compare(UserStatus other) {
        if (this.value > other.value) {
            return 1;
        } else if (this.value == other.value) {
            return 0;
        } else {
            return -1;
        }
    }
}

package com.github.walterfan.msa.common;

/**
 * Created by yafan on 22/7/2017.
 */
public enum TaskPriority {
    IMPORTANT_URGENT(1),
    IMPORTANT_NOT_URGENT(2),
    NOT_IMPORTANT_URGENT(3),
    NOT_IMPORTANT_NOT_URGENT(4);

    private int value;

    TaskPriority(int value) {
        this.value = value;
    }

}

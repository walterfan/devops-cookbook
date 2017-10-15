package com.github.walterfan.msa.common;

/**
 * Created by yafan on 22/7/2017.
 */
public enum TaskType {
    UNDEFINED(0),

    NEXT_ACTION(1),

    ON_SCHEDULE (2),

    WAIT_OTHERS (3);

    private int value;

    TaskType(int value) {
        this.value = value;
    }


}

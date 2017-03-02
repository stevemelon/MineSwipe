package com.example.fanghao01.cccl;

/**
 * Created by fanghao01 on 17/3/1.
 */

public class Mine {
    int value;
    boolean flag;
    boolean isOpen;

    public Mine(int value, boolean flag, boolean isOpen) {
        this.value = value;
        this.flag = flag;
        this.isOpen = isOpen;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}

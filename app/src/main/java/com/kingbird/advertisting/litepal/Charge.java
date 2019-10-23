package com.kingbird.advertisting.litepal;

import org.litepal.crud.LitePalSupport;

/**
 * 充电数据表
 *
 * @author Pan yingdao
 * @date 2019/3/25/025.
 */
public class Charge extends LitePalSupport {
    private int chargeNumber;
    private int chargeTime;
    private int startTime;
    private int state;
    private int newAddTime;

    public int getChargeNumber() {
        return chargeNumber;
    }

    public void setChargeNumber(int chargeNumber) {
        this.chargeNumber = chargeNumber;
    }

    public int getTime() {
        return chargeTime;
    }

    public void setTime(int time) {
        this.chargeTime = time;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getNewAddTime() {
        return newAddTime;
    }

    public void setNewAddTime(int newAddTime) {
        this.newAddTime = newAddTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }
}

/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.model;

import io.nuls.contract.util.ContractUtil;

import java.util.UUID;

/**
 * @author Niels
 */
public class Transaction {

    private String hash;
    private long blockHeight = -1L;
    private int type;
    private long time;
    private CallContractData txData;
    private String remark;

    public Transaction(String hash, long blockHeight, int type, long time, CallContractData txData, String remark) {
        this.hash = hash;
        this.blockHeight = blockHeight;
        this.type = type;
        this.time = time;
        this.txData = txData;
        this.remark = remark;
    }

    public static Transaction newInstance(String sender, String contractAddress, long value, String methodName, Object[] args) {
        String hash = UUID.randomUUID().toString();
        long blockHeight = 1111L;
        int type = 101;
        long time = System.currentTimeMillis();
        CallContractData txData = new CallContractData();
        txData.setSender(sender);
        txData.setContractAddress(contractAddress);
        txData.setValue(value);
        txData.setGasLimit(10000000L);
        txData.setPrice(25L);
        txData.setMethodName(methodName);
        String[][] args2 = ContractUtil.twoDimensionalArray(args);
        txData.setArgsCount((byte) args.length);
        txData.setArgs(args2);
        String remark = "test multy thread contract";
        Transaction tx = new Transaction(hash, blockHeight, type, time, txData, remark);
        return tx;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public ContractData getTxData() {
        return txData;
    }

    public void setTxData(CallContractData txData) {
        this.txData = txData;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.model;

import io.nuls.contract.dto.ContractTransfer;
import io.nuls.kernel.utils.AddressTool;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author: PierreLuo
 * @date: 2018/11/19
 */
@Getter
@Setter
public class ContractResult {

    private String txHash;
    private Transaction tx;
    private byte[] sender;
    private byte[] contractAddress;
    private String result;
    private long gasUsed;
    private long price;
    private byte[] stateRoot;
    private long value;
    private boolean revert;
    private boolean error;
    private String errorMessage;
    private String stackTrace;
    private BigInteger balance;
    private BigInteger nonce;
    private List<ContractTransfer> transfers = new ArrayList<>();
    private List<String> events = new ArrayList<>();
    private String remark;
    private boolean isTerminated;
    private Set<String> contractAddressInnerCallSet;
    private transient Object txTrack;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContractResult)) return false;

        ContractResult result = (ContractResult) o;

        if (getTxHash() != null ? !getTxHash().equals(result.getTxHash()) : result.getTxHash() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getTxHash() != null ? getTxHash().hashCode() : 0;
        return result;
    }

    public boolean isSuccess() {
        return !error && !revert;
    }

    public static ContractResult getFailed(ContractData contractData) {
        ContractResult contractResult = new ContractResult();
        contractResult.setContractAddress(AddressTool.getAddress(contractData.getContractAddress()));
        contractResult.setGasUsed(0L);
        contractResult.setPrice(contractData.getPrice());
        contractResult.setSender(AddressTool.getAddress(contractData.getSender()));
        contractResult.setError(true);
        contractResult.setRevert(true);
        return contractResult;
    }

    public static ContractResult getFailed(ContractData contractData, String msg) {
        ContractResult result = getFailed(contractData);
        result.setErrorMessage(msg);
        return result;
    }
}

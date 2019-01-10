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
package io.nuls.callable;

import io.nuls.kernel.model.Result;
import io.nuls.model.CallableResult;
import io.nuls.model.ContractData;
import io.nuls.model.ContractResult;
import io.nuls.model.Transaction;
import io.nuls.service.ContractVM;
import lombok.Getter;
import lombok.Setter;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author: PierreLuo
 * @date: 2018/11/19
 */
@Getter
@Setter
public class ContractTxCallable implements Callable<CallableResult> {

    private ContractVM contractVM;
    private String contract;
    private List<Transaction> txList;
    private long number;
    private String preStateRoot;


    public ContractTxCallable(String contract, List<Transaction> txList, long number, String preStateRoot) {
        this.contract = contract;
        this.txList = txList;
        this.number = number;
        this.preStateRoot = preStateRoot;
    }

    @Override
    public CallableResult call() throws Exception {
        CallableResult callableResult = CallableResult.newInstance();
        List<ContractResult> resultList = new ArrayList<>();
        callableResult.setContract(contract);
        callableResult.setResultList(resultList);

        ContractData contractData;
        contractVM.createBatchExecute(Hex.decode(preStateRoot));
        for(Transaction tx : txList) {
            contractData = tx.getTxData();
            switch (tx.getType()) {
                case 100 :
                    resultList.add(contractVM.create(contractData, number, preStateRoot));
                    break;
                case 101 :
                    resultList.add(contractVM.call(contractData, number, preStateRoot));
                    break;
                case 102 :
                    resultList.add(contractVM.delete(contractData, number, preStateRoot));
                    break;
                default:
                    break;
            }
        }
        //TODO 批量提交，需要把这一次的`localProgramExecutor`拿出来，最后几个线程的结果都出来后一起提交
        //Result<byte[]> result = contractVM.commitBatchExecute();
        //contractVM.removeBatchExecute();
        return callableResult;
    }

}

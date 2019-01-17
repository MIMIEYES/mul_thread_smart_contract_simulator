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
package io.nuls.service.impl;

import io.nuls.callable.ContractTxCallable;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.executor.ContractExecutor;
import io.nuls.helper.ContractConflictChecker;
import io.nuls.model.*;
import io.nuls.service.ContractCaller;
import io.nuls.service.ContractVM;
import org.spongycastle.util.encoders.Hex;

import java.util.*;
import java.util.concurrent.Future;

/**
 * @author: PierreLuo
 * @date: 2018/11/19
 */
public class ContractCallerImpl implements ContractCaller {

    private ContractVM contractVM;


    @Override
    public CallerResult caller(Map<String, List<Transaction>> txMap, long number, String preStateRoot) {

        List<CallableResult> resultList = new ArrayList<>();
        CallerResult callerResult = new CallerResult();
        try {
            ProgramExecutor batchExecute = contractVM.createBatchExecute(Hex.decode(preStateRoot));
            callerResult.setCallableResultList(resultList);
            callerResult.setProgramExecutor(batchExecute);

            ContractExecutor contractExecutor = ContractExecutor.newInstance();

            ContractConflictChecker checker = ContractConflictChecker.newInstance();
            Set<String>[] sets = new Set[txMap.size()];
            checker.setContractSetArray(sets);
            Set<Map.Entry<String, List<Transaction>>> entries = txMap.entrySet();
            Set<String> commitSet;

            int i = 0;
            for(Map.Entry<String, List<Transaction>> addressTxs : entries) {
                commitSet = new HashSet<>();
                sets[i++] = commitSet;
                String contract = addressTxs.getKey();
                List<Transaction> txList = addressTxs.getValue();
                contractExecutor.add(new ContractTxCallable(batchExecute, contract, txList, number, preStateRoot, checker, commitSet));
            }

            List<Future<CallableResult>> executeList = contractExecutor.execute();
            if(executeList == null) {
                return callerResult;
            }
            for(Future<CallableResult> future : executeList) {
                CallableResult callableResult = future.get();
                resultList.add(callableResult);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return callerResult;
    }

    @Override
    public List<ContractResult> callerReCallTx(ProgramExecutor batchExecutor, List<Transaction> reCallTxList, long number, String preStateRoot) {
        List<ContractResult> resultList = new ArrayList<>();
        ContractData contractData;
        for(Transaction tx : reCallTxList) {
            contractData = tx.getTxData();
            switch (tx.getType()) {
                case 100 :
                    resultList.add(contractVM.create(batchExecutor, contractData, number, preStateRoot));
                    break;
                case 101 :
                    resultList.add(contractVM.call(batchExecutor, contractData, number, preStateRoot));
                    break;
                case 102 :
                    resultList.add(contractVM.delete(batchExecutor, contractData, number, preStateRoot));
                    break;
                default:
                    break;
            }
        }
        return resultList;
    }

    @Override
    public Result<byte[]> commitBatchExecute(ProgramExecutor executor) {
        if(executor == null) {
            return Result.getSuccess();
        }
        executor.commit();
        byte[] stateRoot = executor.getRoot();
        return Result.getSuccess().setData(stateRoot);
    }

}

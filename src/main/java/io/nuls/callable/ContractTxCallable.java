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

import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.core.tools.log.Log;
import io.nuls.helper.ContractConflictChecker;
import io.nuls.model.CallableResult;
import io.nuls.model.ContractData;
import io.nuls.model.ContractResult;
import io.nuls.model.Transaction;
import io.nuls.service.ContractVM;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static io.nuls.utils.ContractUtil.*;

/**
 * @author: PierreLuo
 * @date: 2018/11/19
 */
@Getter
@Setter
public class ContractTxCallable implements Callable<CallableResult> {

    private ContractVM contractVM;
    private ProgramExecutor executor;
    private String contract;
    private List<Transaction> txList;
    private long number;
    private String preStateRoot;
    private ContractConflictChecker checker;
    private Set<String> commitSet;


    public ContractTxCallable(ProgramExecutor executor, String contract, List<Transaction> txList, long number, String preStateRoot, ContractConflictChecker checker, Set<String> commitSet) {
        this.executor = executor;
        this.contract = contract;
        this.txList = txList;
        this.number = number;
        this.preStateRoot = preStateRoot;
        this.checker = checker;
        this.commitSet = commitSet;
    }

    @Override
    public CallableResult call() throws Exception {
        CallableResult callableResult = CallableResult.newInstance();
        List<ContractResult> resultList = callableResult.getResultList();
        callableResult.setContract(contract);

        ContractData contractData;
        // 创建合约无论成功与否，后续的其他的跳过执行，视作失败 -> 合约锁定中或者合约不存在
        // 删除合约成功后，后续的其他的跳过执行，视作失败 -> 合约已删除
        boolean hasCreate = false;
        boolean isDelete = false;
        ContractResult contractResult;
        for(Transaction tx : txList) {
            contractData = tx.getTxData();
            if(hasCreate) {
                resultList.add(ContractResult.getFailed(contractData, "contract lock or not exist."));
                continue;
            }
            if(isDelete) {
                resultList.add(ContractResult.getFailed(contractData, "contract has been terminated."));
                continue;
            }
            switch (tx.getType()) {
                case 100 :
                    hasCreate = true;
                    contractResult = contractVM.create(executor, contractData, number, preStateRoot);
                    checkCreateResult(tx, callableResult, contractResult);
                    break;
                case 101 :
                    contractResult = contractVM.call(executor, contractData, number, preStateRoot);
                    checkCallResult(tx, callableResult, contractResult);
                    break;
                case 102 :
                    contractResult = contractVM.delete(executor, contractData, number, preStateRoot);
                    isDelete = checkDeleteResult(tx, callableResult, contractResult);
                    break;
                default:
                    break;
            }
        }
        return callableResult;
    }

    private void checkCreateResult(Transaction tx, CallableResult callableResult, ContractResult contractResult) {
        if(contractResult.isSuccess()) {
            commitSet.add(contract);
        }
        List<ContractResult> resultList = callableResult.getResultList();
        resultList.add(contractResult);
    }


    private void checkCallResult(Transaction tx, CallableResult callableResult, ContractResult contractResult) {
        boolean isConflict = checker.checkConflictAndCommit(tx, contractResult, commitSet);
        List<ContractResult> resultList = callableResult.getResultList();
        List<ContractResult> reCallList = callableResult.getReCallList();
        if(isConflict) {
            // 冲突后，添加到重新执行的集合中
            reCallList.add(contractResult);
        } else {
            // 没有冲突
            if(contractResult.isSuccess()) {
                // 执行成功，检查与执行失败的交易是否有冲突，把执行失败的交易添加到重新执行的集合中
                checkConflictWithFailedMap(callableResult, contractResult);
                // 本合约与成功执行的其他合约没有冲突，提交本合约
                resultList.add(contractResult);
                commitContract(contractResult);
            } else {
                // 执行失败，添加到执行失败的集合中
                putAll(callableResult.getFailedMap(), contractResult);
            }
        }
    }

    private void commitContract(ContractResult contractResult) {
        Object txTrackObj = contractResult.getTxTrack();
        if(txTrackObj != null && txTrackObj instanceof ProgramExecutor) {
            ProgramExecutor txTrack = (ProgramExecutor) txTrackObj;
            txTrack.commit();
        }
    }

    private void checkConflictWithFailedMap(CallableResult callableResult, ContractResult contractResult) {
        Map<String, Set<ContractResult>> failedMap = callableResult.getFailedMap();
        Set<String> addressSet = collectAddress(contractResult);
        List<ContractResult> reCallList = callableResult.getReCallList();
        for(String address : addressSet) {
            reCallList.addAll(failedMap.remove(address));
        }
    }

    private boolean checkDeleteResult(Transaction tx, CallableResult callableResult, ContractResult contractResult) {
        boolean result = false;
        if(contractResult.isSuccess()) {
            result = true;
        }
        List<ContractResult> resultList = callableResult.getResultList();
        resultList.add(contractResult);
        return result;
    }
}

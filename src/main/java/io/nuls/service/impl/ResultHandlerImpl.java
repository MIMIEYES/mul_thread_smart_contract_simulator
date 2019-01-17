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

import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.model.AnalyzerResult;
import io.nuls.model.ContractResult;
import io.nuls.model.Transaction;
import io.nuls.service.ContractCaller;
import io.nuls.service.ResultHanlder;
import io.nuls.utils.BeanContext;
import io.nuls.utils.CompareTx;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: PierreLuo
 * @date: 2018/11/23
 */
public class ResultHandlerImpl implements ResultHanlder {

    private ContractCaller contractCaller = BeanContext.getBean(ContractCaller.class);

    @Override
    public List<ContractResult> handleAnalyzerResult(ProgramExecutor batchExecutor, AnalyzerResult analyzerResult, long number, String preStateRoot) {

        // 得到重新执行的合约结果
        List<ContractResult> reCallResultList = this.reCall(batchExecutor, analyzerResult, number, preStateRoot);
        // 组装所以的合约结果
        List<ContractResult> finalResultList = new ArrayList<>();
        finalResultList.addAll(analyzerResult.getSuccessList());
        finalResultList.addAll(analyzerResult.getFailedList());
        finalResultList.addAll(reCallResultList);
        // 按时间排序
        return finalResultList.stream().sorted(CompareTx.getInstance()).collect(Collectors.toList());
    }

    private List<ContractResult> reCall(ProgramExecutor batchExecutor, AnalyzerResult analyzerResult, long number, String preStateRoot) {
        // 重新执行合约
        List<ContractResult> list = analyzerResult.getReCallTxList();
        List<Transaction> collect = list.stream().map(c -> c.getTx()).collect(Collectors.toList());
        List<ContractResult> resultList = contractCaller.callerReCallTx(batchExecutor, collect, number, preStateRoot);
        return resultList;
    }
}

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

import io.nuls.model.*;
import io.nuls.service.*;
import io.nuls.utils.BeanContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * @author: PierreLuo
 * @date: 2018/11/19
 */
public class ContractServiceImpl implements ContractService {

    private AddressDistribution addressDistribution = BeanContext.getBean(AddressDistribution.class);

    private ContractCaller contractCaller = BeanContext.getBean(ContractCaller.class);

    private ResultAnalyzer resultAnalyzer = BeanContext.getBean(ResultAnalyzer.class);

    private ResultHanlder resultHanlder = BeanContext.getBean(ResultHanlder.class);

    @Override
    public Result invokeContract(List<Transaction> txList, long number, String preStateRoot) {
        Map<String, List<Transaction>> listMap = addressDistribution.distribution(txList);
        List<CallableResult> callableResultList = contractCaller.caller(listMap, number, preStateRoot);
        AnalyzerResult analyzerResult = resultAnalyzer.analysis(callableResultList);
        List<ContractResult> resultList = resultHanlder.handleAnalyzerResult(callableResultList, analyzerResult, number, preStateRoot);
        return Result.getSuccess().setData(resultList);
    }

}

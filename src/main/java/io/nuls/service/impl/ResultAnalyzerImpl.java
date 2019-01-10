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

import io.nuls.contract.dto.ContractTransfer;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.model.AnalyzerResult;
import io.nuls.model.CallableResult;
import io.nuls.model.ContractResult;
import io.nuls.service.ResultAnalyzer;
import io.nuls.utils.CompareTx;
import io.nuls.utils.ContractUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: PierreLuo
 * @date: 2018/11/19
 */
public class ResultAnalyzerImpl implements ResultAnalyzer {

    /**
     * @param callableResultList
     * @return 需要重新执行智能合约的交易
     */
    @Override
    public AnalyzerResult analysis(List<CallableResult> callableResultList) {
        Set<String> isTerminatedContractAddressSet = extractTerminatedContract(callableResultList);
        List<ContractResult> resultList = extractReCall(callableResultList, isTerminatedContractAddressSet);
        AnalyzerResult analyzerResult = new AnalyzerResult();
        analyzerResult.setReCallTxList(resultList);
        return analyzerResult;
    }

    /**
     * 终止合约的执行结果处理，如果是已终止，那么后面的交易都视作终止，不再重新执行，执行过的合约也作废，变更为失败
     * @param callableResultList
     * @return
     */
    private Set<String> extractTerminatedContract(List<CallableResult> callableResultList) {
        Set<String> result = new HashSet<>();
        for(CallableResult callableResult : callableResultList) {
            List<ContractResult> resultList = callableResult.getResultList();
            boolean isTerminated = false;
            for(ContractResult contractResult : resultList) {
                if(contractResult.isTerminated()) {
                    isTerminated = true;
                    break;
                }
            }
            if(isTerminated) {
                result.add(callableResult.getContract());
            }
            for(ContractResult contractResult : resultList) {
                contractResult.setError(true);
                contractResult.setTerminated(true);
            }
        }
        return result;
    }


    /**
     * @param callableResultList
     * @return 需要重新执行智能合约的交易
     */
    private List<ContractResult> extractReCall(List<CallableResult> callableResultList, Set<String> isTerminatedContractAddressSet) {
        boolean first = true;
        // 收集所有交易的执行结果中的合约地址
        Map<String, Set<ContractResult>> collectAddressMap = null;
        String contract;
        List<ContractResult> needReCallList = new ArrayList<>();
        Set<ContractResult> contractResultSet;
        List<ContractResult> resultList;
        // 集合转换为Map, key -> contractAddress(主合约), value -> contractResultList
        //LinkedHashMap<String, List<ContractResult>> masterCollect = callableResultList.stream().collect(Collectors.toMap(CallableResult::getContract, CallableResult::getResultList, (c1, c2) -> c2, LinkedHashMap::new));

        Map<String, Set<ContractResult>> collectTerminatedAddressMap = new HashMap<>();
        for(CallableResult callableResult : callableResultList) {
            contract = callableResult.getContract();
            resultList = callableResult.getResultList();
            // 把已终止的合约的执行结果收集起来，用于验证其他正常合约中是否在这个集合里有冲突合约，如果有，那么正常合约需要重新执行
            if(isTerminatedContractAddressSet.contains(contract)) {
                putAll(collectTerminatedAddressMap, collectAddress(resultList));
                continue;
            }
            if(first) {
                first = false;
                // 第一次循环，收集这一排的合约地址(每个主合约的所有交易作为一排)
                collectAddressMap = collectAddress(resultList);
                if(collectTerminatedAddressMap.size() > 0) {
                    // 检查这排交易的合约是否和合约终止的收集地址中存在冲突
                    Set<Map.Entry<String, Set<ContractResult>>> entries = collectAddressMap.entrySet();
                    for(Map.Entry<String, Set<ContractResult>> entry : entries) {
                        String contractAddress = entry.getKey();
                        Set<ContractResult> resultSet = entry.getValue();
                        if(collectTerminatedAddressMap.containsKey(contractAddress)) {
                            // 存在冲突，收集地址中的交易重新执行合约
                            needReCallList.addAll(resultSet);
                        }
                    }
                }
                continue;
            }
            // 第二次循环，开始收集这一排不冲突的副合约地址
            boolean allAdded = false;
            // 检查这排交易的主合约是否和收集地址中存在冲突
            if((contractResultSet = collectAddressMap.get(contract)) != null) {
                // 存在冲突，收集地址中的交易和这排所有交易重新执行合约
                allAdded = true;
                needReCallList.addAll(resultList);
                needReCallList.addAll(contractResultSet);
            } else if(collectTerminatedAddressMap.containsKey(contract)){
                // 存在冲突，这排所有交易重新执行合约
                allAdded = true;
                needReCallList.addAll(resultList);
            }
            // 检查这排交易中的副合约是否和收集地址中存在冲突
            for(ContractResult result : resultList) {
                boolean reCall = false;
                for(String address : result.getContractAddressInnerCallSet()) {
                    if((contractResultSet = collectAddressMap.get(address)) != null) {
                        // 存在冲突，此交易以及收集地址的交易需要重新执行合约
                        needReCallList.addAll(contractResultSet);
                        reCall = true;
                    } else if(collectTerminatedAddressMap.containsKey(address)){
                        // 存在冲突，此交易重新执行合约
                        reCall = true;
                    } else {
                        // 不冲突的合约地址添加到整体收集的合约地址Map中
                        put(collectAddressMap, address, result);
                    }
                }
                String transferFromAddress, transferToAddress;
                for(ContractTransfer transfer : result.getTransfers()) {
                    if(ContractUtil.isLegalContractAddress((transferFromAddress = AddressTool.getStringAddressByBytes(transfer.getFrom())))) {
                        if((contractResultSet = collectAddressMap.get(transferFromAddress)) != null) {
                            // 存在冲突，此交易以及收集地址的交易需要重新执行合约
                            needReCallList.addAll(contractResultSet);
                            reCall = true;
                        } else if(collectTerminatedAddressMap.containsKey(transferFromAddress)){
                            // 存在冲突，此交易重新执行合约
                            reCall = true;
                        } else {
                            // 不冲突的合约地址添加到整体收集的合约地址Map中
                            put(collectAddressMap, transferFromAddress, result);
                        }
                    }
                    if(ContractUtil.isLegalContractAddress((transferToAddress = AddressTool.getStringAddressByBytes(transfer.getTo())))) {
                        if((contractResultSet = collectAddressMap.get(transferToAddress)) != null) {
                            // 存在冲突，此交易以及收集地址的交易需要重新执行合约
                            needReCallList.addAll(contractResultSet);
                            reCall = true;
                        } else if(collectTerminatedAddressMap.containsKey(transferToAddress)){
                            // 存在冲突，此交易重新执行合约
                            reCall = true;
                        } else {
                            // 不冲突的合约地址添加到整体收集的合约地址Map中
                            put(collectAddressMap, transferToAddress, result);
                        }
                    }
                }
                if(!allAdded && reCall) {
                    needReCallList.add(result);
                }
            }
        }
        // 去重并排序
        List<ContractResult> list = deduplication(needReCallList);
        return list;
    }


    /**
     * @param needReCallList
     * @return 去掉重复的交易，并按照时间降序排列
     */
    private List<ContractResult> deduplication(List<ContractResult> needReCallList) {
        return needReCallList.stream().collect(Collectors.toSet()).stream()
                .collect(Collectors.toList()).stream().sorted(CompareTx.getInstance()).collect(Collectors.toList());

    }

    /**
     * @param list
     * @return 收集合约执行中所有出现过的合约地址，包括内部调用合约，合约转账
     */
    private static Map<String, Set<ContractResult>> collectAddress(List<ContractResult> list) {
        Map<String, Set<ContractResult>> map = new HashMap<>();
        for(ContractResult result : list) {
            put(map, AddressTool.getStringAddressByBytes(result.getContractAddress()), result);
            //Map<String, ContractResult> collect = result.getContractAddressInnerCallSet().stream().collect(Collectors.toMap(k -> k, v -> result , (s, s2) -> s2, HashMap::new));
            result.getContractAddressInnerCallSet().stream().forEach(inner -> put(map, inner, result));

            result.getTransfers().stream().forEach(transfer -> {
                if(ContractUtil.isLegalContractAddress(transfer.getFrom())) {
                    put(map, AddressTool.getStringAddressByBytes(transfer.getFrom()), result);
                }
                if(ContractUtil.isLegalContractAddress(transfer.getTo())) {
                    put(map, AddressTool.getStringAddressByBytes(transfer.getTo()), result);
                }
            });
        }
        return map;
    }

    private static void put(Map<String, Set<ContractResult>> map, String contractAddress, ContractResult result) {
        Set<ContractResult> resultSet = map.get(contractAddress);
        if(resultSet == null) {
            resultSet = new HashSet<>();
            map.put(contractAddress, resultSet);
        }
        resultSet.add(result);
    }

    private static void putAll(Map<String, Set<ContractResult>> map, Map<String, Set<ContractResult>> collectAddress) {
        Set<Map.Entry<String, Set<ContractResult>>> entries = collectAddress.entrySet();
        for(Map.Entry<String, Set<ContractResult>> entry : entries) {
            String contractAddress = entry.getKey();
            Set<ContractResult> contractResultSet = entry.getValue();

            Set<ContractResult> resultSet = map.get(contractAddress);
            if(resultSet == null) {
                resultSet = new HashSet<>();
                map.put(contractAddress, resultSet);
            }
            resultSet.addAll(contractResultSet);
        }
    }
}

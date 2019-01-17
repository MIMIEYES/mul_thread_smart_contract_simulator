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
package io.nuls.utils;

import io.nuls.kernel.utils.AddressTool;
import io.nuls.model.ContractResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/8/25
 */
public class ContractUtil {


    public static boolean isLegalContractAddress(byte[] addressBytes) {
        if(addressBytes == null) {
            return false;
        }
        return true;
    }

    public static boolean isLegalContractAddress(String address) {
        return true;
    }


    public static void put(Map<String, Set<ContractResult>> map, String contractAddress, ContractResult result) {
        Set<ContractResult> resultSet = map.get(contractAddress);
        if(resultSet == null) {
            resultSet = new HashSet<>();
            map.put(contractAddress, resultSet);
        }
        resultSet.add(result);
    }

    public static void putAll(Map<String, Set<ContractResult>> map, Map<String, Set<ContractResult>> collectAddress) {
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

    public static void putAll(Map<String, Set<ContractResult>> map, ContractResult contractResult) {
        Set<String> addressSet = collectAddress(contractResult);
        for(String address : addressSet) {
            put(map, address, contractResult);
        }
    }

    public static Set<String> collectAddress(ContractResult result) {
        Set<String> set = new HashSet<>();
        set.add(AddressTool.getStringAddressByBytes(result.getContractAddress()));
        set.addAll(result.getContractAddressInnerCallSet());

        result.getTransfers().stream().forEach(transfer -> {
            if(ContractUtil.isLegalContractAddress(transfer.getFrom())) {
                set.add(AddressTool.getStringAddressByBytes(transfer.getFrom()));
            }
            if(ContractUtil.isLegalContractAddress(transfer.getTo())) {
                set.add(AddressTool.getStringAddressByBytes(transfer.getTo()));
            }
        });
        return set;
    }

    /**
     * @param needReCallList
     * @return 去掉重复的交易，并按照时间降序排列
     */
    public static List<ContractResult> deduplicationAndOrder(List<ContractResult> contractResultList) {
        return contractResultList.stream().collect(Collectors.toSet()).stream()
                .collect(Collectors.toList()).stream().sorted(CompareTx.getInstance()).collect(Collectors.toList());
    }

    /**
     * @param list
     * @return 收集合约执行中所有出现过的合约地址，包括内部调用合约，合约转账
     */
    public static Map<String, Set<ContractResult>> collectAddressMap(List<ContractResult> contractResultList) {
        Map<String, Set<ContractResult>> map = new HashMap<>();
        for(ContractResult result : contractResultList) {
            put(map, AddressTool.getStringAddressByBytes(result.getContractAddress()), result);
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
}

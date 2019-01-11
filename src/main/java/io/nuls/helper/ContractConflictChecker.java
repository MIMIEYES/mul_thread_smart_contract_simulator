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
package io.nuls.helper;

import io.nuls.kernel.utils.AddressTool;
import io.nuls.model.ContractResult;
import io.nuls.model.Transaction;
import io.nuls.utils.ContractUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: PierreLuo
 * @date: 2019/1/11
 */
@Getter
@Setter
public class ContractConflictChecker {

    public final ReentrantLock lock = new ReentrantLock();

    public static ContractConflictChecker newInstance() {
        return new ContractConflictChecker();
    }

    private Set<String>[] contractSetArray;

    public boolean checkConflictAndCommit(Transaction tx, ContractResult contractResult, Set<String> commitSet) {
        lock.lock();
        try {
            boolean isConflict = false;
            Set<String> collectAddress = collectAddress(contractResult);
            for(String address : collectAddress) {
                if(containAddress(address, commitSet)) {
                    isConflict = true;
                    break;
                }
            }
            dealResult(tx, contractResult);
            commitSet.addAll(collectAddress);
            return false;
        } finally {
            lock.unlock();
        }

    }

    private void dealResult(Transaction tx, ContractResult contractResult) {
        //TODO 处理结果
    }

    private boolean containAddress(String address, Set<String> commitSet) {
        for(Set<String> set : contractSetArray) {
            if(set == commitSet) {
                continue;
            }
            if(set.contains(address)) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> collectAddress(ContractResult result) {
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

}

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

import io.nuls.model.Transaction;
import io.nuls.service.AddressDistribution;

import java.util.*;

/**
 * @author: PierreLuo
 * @date: 2018/11/19
 */
public class AddressDistributionImpl implements AddressDistribution {

    @Override
    public Map<String, List<Transaction>> distribution(List<Transaction> txList) {
        //TODO change code style to lambda group
        Map<String, List<Transaction>> map = new LinkedHashMap<>();
        for(Transaction tx : txList) {
            String contractAddress = tx.getTxData().getContractAddress();
            List<Transaction> transactions = map.get(contractAddress);
            if(transactions == null) {
                transactions = new ArrayList<>();
                map.put(contractAddress, transactions);
            }
            transactions.add(tx);
        }
        return map;
    }
}

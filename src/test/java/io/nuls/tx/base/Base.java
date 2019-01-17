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
package io.nuls.tx.base;

import io.nuls.model.CallContractData;
import io.nuls.model.ContractData;
import io.nuls.model.Transaction;
import io.nuls.service.*;
import io.nuls.service.impl.*;
import io.nuls.utils.BeanContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * @author: PierreLuo
 * @date: 2018/12/4
 */
public class Base {
    String[] contractAddressSeeds = {
            "NseKMBJ8Z7WgqfDkF5bGKNCUE6G7FMa8",
            "NseM7aGB6QUDo2RrKh9ogbrBC6c27aH6",
            "NseFuqwPHwrZsyBKnhfKQMzSrYsVhjfL",
            "NseBeLaoi4gFEffqsGzB1zVNU6jsjjhK",
            "NseMnV77Tii2nY1Qomuoji5bojgTJbrU",
            "NseBRYKU3vQvZY4XCiSVY5jyL6oVabFr",
            "NseQLXBFZaZotpNhtBQqDLJTtdsoqURd",
            "NsePwaJfYBuVvHxo7YmAfAhcjstoPCXY",
            "NseALLow7yVQyVboH9NreKdccWPGNriq",
            "NseDWcNqN4zpzvLH59o8RjQDReRi5NEH",
            "NseHMCgNK5rCKa3bLVGbVXyd3WnziNxX",
            "NseN7DjnscKKgKH8ckHhAzjHfVapgsz2",
            "NsePjWf23b2UrZnTQwDaSJtqo9AoLC7G",
            "NseL4MPxPCvyTFMo8Qj6bvBJ2NTCJbzN",
            "NseFEH2LC1c9sMzCAHXiuwJtExULZXPt",
            "NseL6yz3co4RzRwaK96P4eL2c7UwuMBE",
            "NseFt39WQKWmUFPQ5bQnGzp6Fzi3z8WD",
            "NseDx4t5u8aR3PoY3YqrxFPJ9bD9rW6F",
            "NseGzHCstPjHEiWGoUJMTse12ZEgPtv7",
            "NseQRVF5rxFGcEH6Mi9Afj8BE5qHcu7D"};

    String[] senderSeeds = {
            "NsdwNp1piSRARnJoV9qLWZtqNULkUXSz",
            "Nsdzh7uPRoMXM4ErrVuQY1AryPyVfWj4",
            "NsdyTeuM3mH5BnAdCLHk3zBGMGUQCqmb",
            "Nse4JUbdj6vrbRGWCzBEoj2WYJvXazS7",
            "NsdtmE55nzonFEyur7Pe9DDNjJqdcnLt",
            "Nse3hf99UwduTKM1qjpGNRxj8bV4XJoi",
            "Nse4NwMo3hwxxtpTkzaB9VGVfhvzPX1x",
            "Nse3hB7auF1fVDPBFKuTf3gqFDDqahtb",
            "NsdyWwhMkWUvJLw8WCEoTRDGp1dc33t9",
            "Nsdy51dGPybGbjGnvfSCk1ZdFQs2wPeK",
            "Nse6h7646A5bSbgXkbANN6oV57j1C6ae",
            "Nse4pEvMHgj2aotePjn2qKR8kiEe8juW",
            "Nse6Pmz3TnEyBmfBDE3mcfd5Z4DyppMG",
            "Nse9fRpc8buRX1cJkd1ySe3ZB41qZUnH",
            "Nse5wr6f1wk5n1CruR1DDaHC9YpdzHEV",
            "Nse1JZzHAY65rAwTFMxsHwum4kDdaxt8",
            "Nse5RVMvgr7iUGzTmZSdmoMidoP7ztvC",
            "NsdvBoGQ6Jstzm228KmzwzvLcqPh2ts9",
            "NsdvhN4gE4UHd5GRaHcyoQPaeKW7ckU3",
            "Nse4JnTguRYsFpwFuDZAsPJuUjXJ5E9n"};

    List<Transaction> list = new ArrayList<>();

    protected void initTx(int txCount) {
        Random random = new Random();
        for (int i = 0; i < txCount; i++) {
            list.add(makeTx(senderSeeds[random.nextInt(21)], contractAddressSeeds[random.nextInt(21)]));
        }
    }

    protected void initBean() {
        try {
            BeanContext.register(AddressDistribution.class, AddressDistributionImpl.class);
            BeanContext.register(ContractCaller.class, ContractCallerImpl.class);
            BeanContext.register(ResultAnalyzer.class, ResultAnalyzerImpl.class);
            BeanContext.register(ResultHanlder.class, ResultHandlerImpl.class);
            BeanContext.register(ContractService.class, ContractServiceImpl.class);
            //TODO VM实现类
            BeanContext.register(ContractVM.class, ContractVM.class);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }

    private Transaction makeTx(String sender, String contractAddress) {
        return new Transaction(UUID.randomUUID().toString(), 555L, 2, System.currentTimeMillis(),
                makeCallContractData(sender, contractAddress), "beizhu");
    }

    private ContractData makeCallContractData(String sender, String contractAddress) {
        CallContractData txData = new CallContractData();
        txData.setSender(sender);
        txData.setGasLimit(123123L);
        txData.setPrice(25L);
        txData.setValue(0L);
        txData.setMethodName("single");
        txData.setMethodDesc(EMPTY);
        txData.setArgsCount((byte) 0);
        txData.setArgs(null);
        txData.setContractAddress(contractAddress);
        return txData;
    }

}

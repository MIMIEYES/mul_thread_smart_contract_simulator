package io.nuls.service.impl;

import io.nuls.model.Result;
import io.nuls.service.ContractCaller;
import io.nuls.service.ContractService;
import io.nuls.tx.base.Base;
import io.nuls.utils.BeanContext;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.*;

public class ContractServiceImplTest extends Base {

    private ContractService service;

    @Before
    public void init() {
        initData();
        service = BeanContext.getBean(ContractService.class);
    }

    @Test
    public void invokeContractTest() {
        Result result = service.invokeContract(this.list, 2, "d868c17e627659aea4d3c722deac13ccef7d13b4203362acf939078bbf00f105");
        System.out.println(Hex.toHexString((byte[]) result.getData()));
    }

}
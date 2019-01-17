package io.nuls.utils;

import org.ethereum.crypto.HashUtil;
import org.junit.Test;
import org.spongycastle.jcajce.provider.digest.Keccak;
import org.spongycastle.jcajce.provider.digest.SHA3;
import org.spongycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

public class KeccakHashTest {

    @Test
    public void test() throws UnsupportedEncodingException {
        String asd = "atool.org";
        byte[] asdBytes = asd.getBytes("UTF-8");
        byte[] ethHashBytes = HashUtil.sha3(asdBytes);

        String localHash0 = Sha3Hash.sha3(asdBytes);
        SHA3.Digest256 digest256 = new SHA3.Digest256();
        Keccak.Digest256 keccak_digest256 = new Keccak.Digest256();

        String localHash1 = Hex.toHexString(digest256.digest(asdBytes));
        String localHash2 = Hex.toHexString(keccak_digest256.digest(asdBytes));
        String localHash3 = KeccakHash.keccak(asdBytes);

        System.out.println("eth: " + Hex.toHexString(ethHashBytes));

        System.out.println("local0-sha3-256: " + localHash0);
        System.out.println("local1-sha3-256: " + localHash1);
        System.out.println("local2-keccak-256: " + localHash2);
        System.out.println("local3-keccak-256: " + localHash3);

    }

}
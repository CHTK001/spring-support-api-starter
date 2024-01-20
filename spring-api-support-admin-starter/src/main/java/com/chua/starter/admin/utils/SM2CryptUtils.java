package com.chua.starter.admin.utils;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.BCUtil;
import cn.hutool.crypto.ECKeyUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.PlainDSAEncoding;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;

import java.util.HashMap;
import java.util.Map;

public class SM2CryptUtils {

    private static String privateKey="";
    private static String publicKey="";

    //生成秘钥对
    public static Map<String,String> createSM2Key(){
        SM2 sm2=SmUtil.sm2();
        sm2.setMode(SM2Engine.Mode.C1C3C2);
        privateKey=HexUtil.encodeHexStr(BCUtil.encodeECPrivateKey(sm2.getPrivateKey()));
        publicKey = HexUtil.encodeHexStr(((BCECPublicKey) sm2.getPublicKey()).getQ().getEncoded(false));
        Map<String,String> keys=new HashMap<>();
        keys.put(privateKey,publicKey);
        return keys;
    }

    //加密
    public static String encrypt(String data){
        String publicKeyTmp = publicKey;
        if (publicKey.length() == 130) {
            //这里需要去掉开始第一个字节 第一个字节表示标记
            publicKeyTmp = publicKey.substring(2);
        }
        String xhex = publicKeyTmp.substring(0, 64);
        String yhex = publicKeyTmp.substring(64, 128);
        ECPublicKeyParameters ecPublicKeyParameters = BCUtil.toSm2Params(xhex, yhex);
        //创建sm2 对象
        SM2 sm2 = new SM2(null, ecPublicKeyParameters);
        sm2.usePlainEncoding();
        sm2.setMode(SM2Engine.Mode.C1C3C2);
        String hex = sm2.encryptHex(data,KeyType.PublicKey).substring(2);
        return hex;

    }

    //解密
    public static String decrypt(String data){
        //ECKeyUtil.toSm2PrivateParams()
        SM2 sm2 = new SM2(ECKeyUtil.toSm2PrivateParams(privateKey), null);
        sm2.setMode(SM2Engine.Mode.C1C3C2);
        sm2.setEncoding(new PlainDSAEncoding());
        String encryptStr = sm2.decryptStr("04"+data, KeyType.PrivateKey);
        return encryptStr;
    }
}
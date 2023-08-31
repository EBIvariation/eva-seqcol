package uk.ac.ebi.eva.evaseqcol.io;

import org.springframework.stereotype.Component;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.utils.JSONLevelOne;

import java.util.Arrays;

/**
 * Generate some small seqCol objects examples for testing purposes*/
@Component
public class SeqColGenerator {

    /**
     * Return an example (might not be real) of a seqCol object level 1
     * The naming convention is set to GENBANK as a random choice*/
    public SeqColLevelOneEntity generateLevelOneEntity() {
        SeqColLevelOneEntity levelOneEntity = new SeqColLevelOneEntity();
        JSONLevelOne jsonLevelOne = new JSONLevelOne();
        jsonLevelOne.setNames("mfxUkK3J5y7BGVW7hJWcJ3erxuaMX6xm");
        jsonLevelOne.setSequences("dda3Kzi1Wkm2A8I99WietU1R8J4PL-D6");
        jsonLevelOne.setLengths("Ms_ixPgQMJaM54dVntLWeovXSO7ljvZh");
        jsonLevelOne.setMd5DigestsOfSequences("_6iaYtcWw4TZaowlL7_64Wu9mbHpDUw4");
        jsonLevelOne.setSortedNameLengthPairs("QFuKs5Hh8uQwwUtnRxIf8W3zeJoFOp8Z");
        levelOneEntity.setSeqColLevel1Object(jsonLevelOne);
        levelOneEntity.setDigest("3mTg0tAA3PS-R1TzelLVWJ2ilUzoWfVq");
        levelOneEntity.setNamingConvention(SeqColEntity.NamingConvention.GENBANK);
        return levelOneEntity;
    }

    /**
     * Return an example (might not be real) of a seqCol object level 2
     * The naming convention is set to GENBANK as a random choice
     * */
    public SeqColLevelTwoEntity generateLevelTwoEntity() {
        SeqColLevelTwoEntity levelTwoEntity = new SeqColLevelTwoEntity();
        levelTwoEntity.setSequences(Arrays.asList(
                "SQ.lZyxiD_ByprhOUzrR1o1bq0ezO_1gkrn",
                "SQ.vw8jTiV5SAPDH4TEIZhNGylzNsQM4NC9",
                "SQ.A_i2Id0FjBI-tQyU4ZaCEdxRzQheDevn",
                "SQ.QXSUMoZW_SSsCCN9_wc-xmubKQSOn3Qb",
                "SQ.UN_b-wij0EtsgFqQ2xNsbXs_GYQQIbeQ",
                "SQ.z-qJgWoacRBV77zcMgZN9E_utrdzmQsH",
                "SQ.9wkqGXgK6bvM0gcjBiTDk9tAaqOZojlR",
                "SQ.K8ln7Ygob_lcVjNh-C8kUydzZjRt3UDf",
                "SQ.hb1scjdCWL89PtAkR0AVH9-dNH5R0FsN",
                "SQ.DKiPmNQT_aUFndwpRiUbgkRj4DPHgGjd",
                "SQ.RwKcMXVadHZub1qL0Y5c1gmNU1_vHFme",
                "SQ.1sw7ZtgO9JRb1kUEuhVz1wBix5_8Opci",
                "SQ.V7DQqMKG7bcyxiMZK9wNjkK-udR7hrad",
                "SQ.R8nT1N2qQFMc_uVMQUVMw-D2GcVmb5v6",
                "SQ.DPa_ORXLkGyyCbW9SWeqePfortM-Vdlm",
                "SQ.koyLEKoDOQtGHjb4r0m3o2SXxI09Z_sI"
        ));
        levelTwoEntity.setNames(Arrays.asList(
                "I",
                "II",
                "III",
                "IV",
                "V",
                "VI",
                "VII",
                "VIII",
                "IX",
                "X",
                "XI",
                "XII",
                "XIII",
                "XIV",
                "XV",
                "XVI"
        ));
        levelTwoEntity.setLengths(Arrays.asList(
                "230218",
                "813184",
                "316620",
                "1531933",
                "576874",
                "270161",
                "1090940",
                "562643",
                "439888",
                "745751",
                "666816",
                "1078177",
                "924431",
                "784333",
                "1091291",
                "948066"
        ));
        levelTwoEntity.setMd5DigestsOfSequences(Arrays.asList(
                "6681ac2f62509cfc220d78751b8dc524",
                "97a317c689cbdd7e92a5c159acd290d2",
                "54f4a74aa6392d9e19b82c38aa8ab345",
                "74180788027e20df3de53dcb2367d9e3",
                "d2787193198c8d260f58f2097f9e1e39",
                "b7ebc601f9a7df2e1ec5863deeae88a3",
                "a308c7ebf0b67c4926bc190dc4ba8ed8",
                "f66a4f8eef89fc3c3a393fe0210169f1",
                "4eae53ae7b2029b7e1075461c3eb9aac",
                "6757b8c7d9cca2c56401e2484cf5e2fb",
                "e72df2471be793f8aa06850348a896fa",
                "77945d734ab92ad527d8920c9d78ac1c",
                "073f9ff1c599c1a6867de2c7e4355394",
                "188bca5110182a786cd42686ec6882c6",
                "7e02090a38f05459102d1a9a83703534",
                "232475e9a61a5e07f9cb2df4a2dad757"
        ));
        levelTwoEntity.setSortedNameLengthPairs(Arrays.asList(
                "cyPiAVRfjCEi1sF8dEyZfmHhAhAC0SF4",
                "dtN6kCT5Ox-kMxz6MYCF2MJ-ILGS6WQK",
                "DWcOfnB22R2Unj-CyIYq4E_mLkHFTex1",
                "luygeEnb7JRCxOmRNbF0Ubkk24pPzk4t",
                "o-UYrPRs2LFPiSmPNsnRO8rSKAguWQqB",
                "Qpewc4624t9ndYzXjvfsSdgpuTtbky-Y",
                "qQQM5JdawzMIv3uQIOG7f_e5DoVoSLvS",
                "R6_wJzH4nrTjLfUKPZi4ovVakAglE1y3",
                "Tw5BP1nJnjAdjYd_-xrQHfAtRKRY-Vkq",
                "uqCztWdAKSw80eSuGRny5Kz9eyohLg4u",
                "wbXyAopkgDj0JxGY_KiNGDGxT-G5mOaf",
                "X4QMYkoboes8cY399ZhGx5DFIgigrkyP",
                "xjgIV09f89NER4GZ6wfypBhAKVp-96g9",
                "xmBmTgWnrnLQfApAZAHfCCwXz4BNfVR6",
                "y2k5ZWUuGU0uPdyJXy-rp4DJbH-X9EeJ",
                "YfHZgnpuJm4SN3RN4XL1VWWWZwTXtqw5"
        ));
        levelTwoEntity.setDigest("3mTg0tAA3PS-R1TzelLVWJ2ilUzoWfVq");
        levelTwoEntity.setNamingConvention(SeqColEntity.NamingConvention.GENBANK);
        return levelTwoEntity;
    }
}

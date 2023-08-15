package uk.ac.ebi.eva.evaseqcol.io;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.utils.JSONLevelOne;

import java.util.Arrays;

/**
 * Generate some small seqCol objects examples for testing purposes*/
public class SeqColGenerator {

    /**
     * Return an example (might not be real) of a seqCol object level 1
     * The naming convention is set to GENBANK as a random choice*/
    public SeqColLevelOneEntity generateLevelOneEntity() {
        SeqColLevelOneEntity levelOneEntity = new SeqColLevelOneEntity();
        JSONLevelOne jsonLevelOne = new JSONLevelOne();
        jsonLevelOne.setNames("g04lKdxiYtG3dOGeUC5AdKEifw65G0Wp");
        jsonLevelOne.setSequences("EiYgJtUfGyad7wf5atL5OG4Fkzohp2qe");
        jsonLevelOne.setLengths("5K4odB173rjao1Cnbk5BnvLt9V7aPAa2");
        jsonLevelOne.setMd5DigestsOfSequences("sdf25fsdf2sdf7sd2f87sdf72sdfqsd");
        jsonLevelOne.setSortedNameLengthPairs("sdfssmksdqsqsmf358shj87fg528qsdDSFsd");
        levelOneEntity.setSeqColLevel1Object(jsonLevelOne);
        levelOneEntity.setDigest("S3LCyI788LE6vq89Tc_LojEcsMZRixzP");
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
                "jh86dq1sd3gfdSDFGag25sd4f",
                "sdmgdsf687jhh2g1sqsdqpoze",
                "d4sfg6h8qdfg5857qsdfponez"
        ));
        levelTwoEntity.setNames(Arrays.asList("A", "B", "C"));
        levelTwoEntity.setLengths(Arrays.asList("1216", "970", "1788"));
        levelTwoEntity.setMd5Sequences(Arrays.asList("MD5-sqdfsdodshijfsd354768",
                                                     "MD5-fjroptkgqsdfsd5f7sdlp",
                                                     "MD5-sdpohgnjkisqdj,fiokjz"));
        levelTwoEntity.setSortedNameLengthPairs(Arrays.asList("sdqgoi687687gfhqzsQSFg",
                                                              "poiu5768fdQSDGqsdfpoif",
                                                              "oijpSDFpfdijsdkgmfl357"));
        levelTwoEntity.setDigest("FGJQSdfpeizjf527sdfhrtpoq");
        levelTwoEntity.setNamingConvention(SeqColEntity.NamingConvention.GENBANK);
        return levelTwoEntity;
    }
}

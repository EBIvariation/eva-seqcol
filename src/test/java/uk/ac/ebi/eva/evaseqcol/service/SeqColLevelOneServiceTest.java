package uk.ac.ebi.eva.evaseqcol.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import uk.ac.ebi.eva.evaseqcol.refget.ChecksumCalculator;
import uk.ac.ebi.eva.evaseqcol.refget.MD5Calculator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SeqColLevelOneServiceTest {


    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void addSequenceCollectionL1() {
    }

    @Test
    void getSeqColL1ByDigest() {
    }

    @Test
    void getAllSeqCollections() {
    }
}
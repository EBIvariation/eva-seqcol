package uk.ac.ebi.eva.evaseqcol.entities;

import lombok.Data;

import java.util.List;

@Data
public class SeqColLevelTwoEntity extends SeqColEntity{

    private List<String> sequences;
    private List<String> names;
    private List<String> lengths;

}

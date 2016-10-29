package de.sudoq.model.solverGenerator.solution;

import java.util.BitSet;
import java.util.List;
import java.util.Stack;

import de.sudoq.model.solvingAssistant.HintTypes;
import de.sudoq.model.sudoku.CandidateSet;
import de.sudoq.model.sudoku.Constraint;

/**
 * Created by timo on 04.10.16.
 */
public class NakedSetDerivation extends SolveDerivation {


    private Constraint constraint;
    private List<DerivationField> subsetMembers;
    private List<DerivationField> externalFields;
    private CandidateSet subsetCandidates;

    public NakedSetDerivation(HintTypes technique) {
        super(technique);
        subsetMembers  = new Stack<>();
        externalFields = new Stack<>();
    }

    public void setSubsetCandidates(CandidateSet bs){
        subsetCandidates = (CandidateSet) bs.clone();
    }

    public void setConstraint(Constraint c){ constraint = c; }

    public void addExternalField(DerivationField f){
        externalFields.add(f);
    }

    public void addSubsetField(DerivationField f){
        subsetMembers.add(f);
    }


    public CandidateSet getSubsetCandidates(){ return subsetCandidates; }

    public Constraint getConstraint(){ return constraint; }

    public List<DerivationField> getSubsetMembers(){ return subsetMembers; }

    public List<DerivationField> getExternalFieldsMembers(){ return externalFields; }

}

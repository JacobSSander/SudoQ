package de.sudoq.model.solverGenerator.solver.helper;

import java.util.Vector;

import de.sudoq.model.solverGenerator.solution.LastCandidateDerivation;
import de.sudoq.model.solverGenerator.solver.SolverSudoku;
import de.sudoq.model.solvingAssistant.HintTypes;
import de.sudoq.model.sudoku.Constraint;
import de.sudoq.model.sudoku.Position;

public class LastCandidateHelper extends SolveHelper
{
	LastCandidateDerivation derivation;
	
	public LastCandidateHelper(SolverSudoku sudoku, int complexity) throws IllegalArgumentException
	{
		super(sudoku, complexity);
		hintType = HintTypes.LastCandidate;
	}
	
	@Override
	public boolean update(boolean buildDerivation)
	{
		//iterate through all positions of all constraints, none twice
		Vector<Position> seen = new Vector<>();
		for(Constraint c : sudoku.getSudokuType())
		{
			if(c.hasUniqueBehavior())
			{
				for(Position p : c)
				{
					if(!seen.contains(p))
					{
						seen.add(p);
						
						if(sudoku.getCurrentCandidates(p).cardinality() == 1)
						{
							int lastNote = sudoku.getCurrentCandidates(p).nextSetBit(0);
							derivation = new LastCandidateDerivation(p, lastNote);
							lastDerivation = derivation;
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
}

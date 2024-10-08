package de.sudoq.model.solverGenerator.solver.helper;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import de.sudoq.model.solverGenerator.solution.DerivationCell;
import de.sudoq.model.solverGenerator.solution.NoNotesDerivation;
import de.sudoq.model.solverGenerator.solver.SolverSudoku;
import de.sudoq.model.solvingAssistant.HintTypes;
import de.sudoq.model.sudoku.Cell;
import de.sudoq.model.sudoku.Constraint;
import de.sudoq.model.sudoku.Position;

public class NoNotesHelper extends SolveHelper
{
	public NoNotesHelper(SolverSudoku sudoku, int complexity) throws IllegalArgumentException
	{
		super(sudoku, complexity);
		hintType = HintTypes.NoNotes;
	}
	
	@Override
	public boolean update(boolean buildDerivation)
	{
		boolean foundOne = false;
		Position candidate;
		Vector<Position> emptyPos = new Vector<>();
		for(Position p : sudoku.getSudokuType().getValidPositions())
		{
			if(sudoku.getCell(p).isCompletelyEmpty())
			{
				emptyPos.add(p);
			}
		}
		
		foundOne = emptyPos.size() > 0;
		
		if(buildDerivation)
		{
			//Todo replace by filling in all notes and apply other helpers repeatedly? but how can we limit this to allempty positions?
			//create map from pos to constraint
			Set<Position> emptyPosSet = new HashSet<>(emptyPos);
			Map<Position, List<Constraint>> cmap = new HashMap<>();
			for(Constraint c : sudoku.getSudokuType())
			{
				if(c.hasUniqueBehavior())
				{
					for(Position p : c.getPositions())
					{
						if(emptyPosSet.contains(p))
						{
							if(cmap.containsKey(p))
							{
								cmap.get(p).add(c);
							}
							else
							{
								cmap.put(p, new ArrayList<>(Collections.singletonList(c)));
							}
						}
					}
				}
			}
			
			lastDerivation = new NoNotesDerivation();
			Set<Integer> allSymbbols = new HashSet<Integer>((Collection) sudoku.getSudokuType().getSymbolIterator());
			//for(int i : sudoku.getSudokuType().getSymbolIterator())
			//    allSymbbols.add(i);
			
			for(Position p : emptyPos)
			{
				Set<Integer> allCandidates = new HashSet<>(allSymbbols);
				
				for(Constraint c : cmap.get(p))
				{
					for(Position pi : c.getPositions())
					{
						Cell f = sudoku.getCell(pi);
						if(f.isSolved())
						{
							allCandidates.remove(f.getCurrentValue());
						}
					}
				}
				BitSet relevant = new BitSet();
				BitSet irrelevant = new BitSet();
				for(Integer i : allSymbbols)
				{
					if(allCandidates.contains(i))
					{
						relevant.set(i);
					}
					else
					{
						irrelevant.set(i);
					}
				}
				lastDerivation.addDerivationCell(new DerivationCell(p, relevant, irrelevant));
			}
		}
		
		return foundOne;
	}
}

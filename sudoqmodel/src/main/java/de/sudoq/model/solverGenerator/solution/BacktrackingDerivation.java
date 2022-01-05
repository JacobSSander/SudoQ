package de.sudoq.model.solverGenerator.solution;

import de.sudoq.model.solvingAssistant.HintTypes;

public class BacktrackingDerivation extends SolveDerivation
{
	public BacktrackingDerivation()
	{
		technique = HintTypes.Backtracking;
	}
}

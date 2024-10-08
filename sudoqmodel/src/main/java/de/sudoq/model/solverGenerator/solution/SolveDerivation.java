package de.sudoq.model.solverGenerator.solution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.sudoq.model.actionTree.Action;
import de.sudoq.model.solvingAssistant.HintTypes;
import de.sudoq.model.sudoku.Sudoku;

/**
 * Ein Objekt dieser Klasse stellt einen Herleitungsschritt für die Lösung eines
 * Sudoku-Feldes dar. Dazu enthält es eine Liste von DerivationFields und
 * DerivationBlocks, die Informationenen über die entsprechend relevanten
 * Blöcke, sowie Kandidaten in den beteiligten Feldern enthalten.
 */
public /*abstract*/ class SolveDerivation
{
	/* Attributes */
	
	/**
	 * A string holding the name of the technique that led to this derivation
	 */
	protected HintTypes technique;
	
	/**
	 * A textual illustration of this solution step
	 */
	private String description;
	
	/**
	 * Eine Liste von DerivationFields, die für diesen Lösungsschritt relevant sind
	 */
	private List<DerivationCell> cells;
	
	/**
	 * Eine Liste von DerivationBlocks, die für diesen Lösungsschritt relevant sind
	 */
	private List<DerivationBlock> blocks;
	
	protected boolean hasActionListCapability = false;
	
	/* Constructors */
	
	/**
	 * Initiiert ein neues SolveDerivation-Objekt.
	 */
	public SolveDerivation()
	{
		this(null);
	}
	
	public SolveDerivation(HintTypes technique)
	{
		this.technique = technique;
		this.cells = new ArrayList<>();
		this.blocks = new ArrayList<>();
	}
	
	public HintTypes getType()
	{
		return technique;
	}
	
	/* Methods */
	
	/**
	 * Accepts a string description of what this derivation does
	 *
	 * @param descrip String that describes the derivation
	 */
	public void setDescription(String descrip)
	{
		this.description = descrip;
	}
	
	/**
	 * Diese Methode fügt das spezifizierte DerivationField zur Liste der
	 * DerivationFields dieses SolveDerivation-Objektes hinzu. Ist das
	 * übergebene Objekt null, so wird es nicht hinzugefügt.
	 *
	 * @param cell Das DerivationField, welches dieser SolveDerivation hinzugefügt werden soll
	 */
	public void addDerivationCell(DerivationCell cell)
	{
		if(cell != null)
		{
			this.cells.add(cell);
		}
	}
	
	/**
	 * Diese Methode fügt den spezifizierten DerivationBlock zur Liste der
	 * DerivationBlocks dieses SolveDerivation-Objektes hinzu. Ist das übegebene
	 * Objekt null, so wird es nicht hinzugefügt.
	 *
	 * @param block Der DerivationBlock, welcher dieser SolveDerivation hinzugefügt werden soll
	 */
	public void addDerivationBlock(DerivationBlock block)
	{
		if(block == null)
		{
			return;
		}
		this.blocks.add(block);
	}
	
	/**
	 * Diese Methode gibt einen Iterator zurück, mit dem über die diesem Objekt
	 * hinzugefügten DerivationFields iteriert werden kann.
	 *
	 * @return Ein Iterator, mit dem über die DerivationFields dieses SolveDerivation-Objektes iteriert werden kann
	 */
	public Iterator<DerivationCell> getCellIterator()
	{
		return cells.iterator();
	}
	
	/**
	 * Diese Methode gibt einen Iterator zurück, mithilfe dessen über die diesem
	 * Objekt hinzugefügten DerivationBlocks iteriert werden kann.
	 *
	 * @return Ein Iterator, mit dem über die DerivationBlocks dieses SolveDerivation-Objektes iteriert werden kann
	 */
	Iterator<DerivationBlock> getBlockIterator()
	{
		return blocks.iterator();
	}
	
	public List<DerivationBlock> getDerivationBlocks()
	{
		return blocks;
	}
	
	public boolean hasActionListCapability()
	{
		return hasActionListCapability;
	}
	
	public List<Action> getActionList(Sudoku sudoku)
	{
		return new ArrayList<>();
	}
	
	public String toString()
	{
		return description;
	}
}

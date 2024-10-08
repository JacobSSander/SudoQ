/*
 * SudoQ is a Sudoku-App for Adroid Devices with Version 2.2 at least.
 * Copyright (C) 2012  Heiko Klare, Julian Geppert, Jan-Bernhard Kordaß, Jonathan Kieling, Tim Zeitz, Timo Abele
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.sudoq.model.solverGenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

import de.sudoq.model.files.FileManager;
import de.sudoq.model.solverGenerator.FastSolver.BranchAndBound.FastBranchAndBound;
import de.sudoq.model.solverGenerator.FastSolver.FastSolver;
import de.sudoq.model.solverGenerator.FastSolver.FastSolverFactory;
import de.sudoq.model.solverGenerator.solution.SolveDerivation;
import de.sudoq.model.solverGenerator.solver.ComplexityRelation;
import de.sudoq.model.solverGenerator.solver.Solver;
import de.sudoq.model.sudoku.Constraint;
import de.sudoq.model.sudoku.Cell;
import de.sudoq.model.sudoku.Position;
import de.sudoq.model.sudoku.PositionMap;
import de.sudoq.model.sudoku.Sudoku;
import de.sudoq.model.sudoku.SudokuBuilder;
import de.sudoq.model.sudoku.complexity.Complexity;
import de.sudoq.model.sudoku.complexity.ComplexityConstraint;
import de.sudoq.model.sudoku.sudokuTypes.SudokuType;
import de.sudoq.model.sudoku.sudokuTypes.SudokuTypes;
import de.sudoq.model.xml.SudokuXmlHandler;
import de.sudoq.model.xml.XmlAttribute;
import de.sudoq.model.xml.XmlHelper;
import de.sudoq.model.xml.XmlTree;

import static de.sudoq.model.solverGenerator.solver.ComplexityRelation.INVALID;
import static de.sudoq.model.solverGenerator.solver.ComplexityRelation.CONSTRAINT_SATURATION;

/**
 * Bietet die Möglichkeit Sudokus zu generieren.
 * Die Klasse implementiert das {@link Runnable} interface
 * und kann daher in einem eigenen Thread ausgeführt werden.
 */
public class GenerationAlgo implements Runnable
{
	/* Attributes */
	
	/**
	 * Das Sudoku auf welchem die Generierung ausgeführt wird
	 */
	protected Sudoku sudoku;
	
	/**
	 * Das Zufallsobjekt für den Generator
	 */
	protected Random random;
	
	/**
	 * Der Solver, der für Validierungsvorgänge genutzt wird
	 */
	protected Solver solver;
	
	/**
	 * Das Objekt, auf dem nach Abschluss der Generierung die
	 * Callback-Methode aufgerufen wird
	 */
	protected GeneratorCallback callbackObject;
	
	/**
	 * List of currently defined(occupied) Fields.
	 * If we gave the current sudoku to the user tey wouldn't have to solve these fields
	 * as they'd already be filled in.
	 */
	protected List<Position> definedCells;
	
	/**
	 * Die noch freien, also nicht belegten Felder des Sudokus
	 */
	protected List<Position> freeCells;
	
	/**
	 * Das gelöste Sudoku
	 */
	protected Sudoku solvedSudoku;
	
	/**
	 * Die Anzahl der Felder, die fest zu definieren ist
	 */
	private int cellsToDefine;
	
	/**
	 * Anzahl aktuell definierter Felder
	 */
	//private int currentFieldsDefined;
	
	/**
	 * ComplexityConstraint für ein Sudoku des definierten
	 * Schwierigkeitsgrades
	 */
	private ComplexityConstraint desiredComplexityConstraint;
	
	/**
	 * Instanziiert ein neues Generierungsobjekt für das spezifizierte
	 * Sudoku. Da die Klasse privat ist wird keine Überprüfung der
	 * Eingabeparameter durchgeführt.
	 *
	 * @param sudoku
	 *            Das Sudoku, auf dem die Generierung ausgeführt werden soll
	 * @param callbackObject
	 *            Das Objekt, auf dem die Callback-Methode nach Abschluss
	 *            der Generierung aufgerufen werden soll
	 * @param random
	 *            Das Zufallsobjekt zur Erzeugung des Sudokus
	 */
	public GenerationAlgo(Sudoku sudoku, GeneratorCallback callbackObject, Random random)
	{
		this.sudoku = sudoku;
		this.callbackObject = callbackObject;
		this.solver = new Solver(sudoku);
		this.freeCells = new ArrayList<>();
		this.definedCells = new ArrayList<>();
		this.random = random;
		
		this.desiredComplexityConstraint = sudoku.getSudokuType().buildComplexityConstraint(sudoku.getComplexity());
		
		freeCells.addAll(getPositions(sudoku));//fills the currenlty empty list freefields as no field is defined=occupied
	}
	
	/**
	 * Die Methode, die die tatsächliche Generierung eines Sudokus mit der
	 * gewünschten Komplexität generiert.
	 */
	public void run()
	{
		/* 1. Finde Totalbelegung */
		solvedSudoku = createSudokuPattern();
		
		createAllocation(solvedSudoku);
		
		// Call the callback
		SudokuBuilder suBi = new SudokuBuilder(sudoku.getSudokuType());
		
		for(Position p : getPositions(solvedSudoku))
		{
			int value = solvedSudoku.getCell(p).getSolution();
			suBi.addSolution(p, value);
			if(!sudoku.getCell(p).isNotSolved())
			{
				suBi.setFixed(p);
			}
		}
		Sudoku res = suBi.createSudoku();
		
		//we want to know the solutions used, so quickly an additional solver
		Solver quickSolver = new Solver(res);
		quickSolver.solveAll(true, false, false);
		
		res.setComplexity(sudoku.getComplexity());
		if(callbackObject.toString().equals("experiment"))
		{
			callbackObject.generationFinished(res, quickSolver.getSolutions());
		}
		else
		{
			callbackObject.generationFinished(res);
		}
	}
	
	private Sudoku createSudokuPattern()
	{
		//determine ideal number of prefilled fields
		cellsToDefine = getNumberOfCellsToDefine(sudoku.getSudokuType(), desiredComplexityConstraint);
		
		//A mapping from position to solution
		PositionMap<Integer> solution = new PositionMap<>(this.sudoku.getSudokuType().getSize());
		int iteration = 0;
		//System.out.println("Fields to define: "+fieldsToDefine);
		
		//define fields
		for(int i = 0; i < cellsToDefine; i++)
		{
			Position p = addDefinedCell();
		}
		
		int fieldsToDefineDynamic = cellsToDefine;
		
		/* until a solution is found, remove 5 random fields and add new ones */
		FastSolver fs = FastSolverFactory.getSolver(sudoku);
		while(!fs.hasSolution())
		{
			//System.out.println("Iteration: "+(iteration++)+", defined Fields: "+definedFields.size());
			// Remove some fields, because sudoku could not be validated
			removeDefinedCells(5);
			
			// Define average number of fields
			while(definedCells.size() < fieldsToDefineDynamic)
			{
				if(addDefinedCell() == null) //try to add field, if null returned i.e. nospace / invalid
				{
					removeDefinedCells(5); //remove 5 fields
				}
			}
			
			if(fieldsToDefineDynamic > 0 && random.nextFloat() < 0.2)
			{
				fieldsToDefineDynamic--; //to avoid infinite loop slowly relax
			}
			fs = FastSolverFactory.getSolver(sudoku);
		}
		
		/* we found a solution i.e. a combination of nxn numbers that fulfill all constraints */

		/* not sure what's happening why tmp-save complexity? is it ever read? maybe in solveall?
		   maybe this is from previous debugging, wanting to see if it's invalid here already

		   solver.validate is definitely needed

		   but the complexity is from the superclass `Sudoku`, SolverSudoku has its own `complexityValue`...
		*/
		
		//PositionMap<Integer> solution2 = solver.getSudoku().getField();
		
		//solution is filled with correct solution
		solution = fs.getSolutions();
		
		/* We have (validated) filled `solution` with the right values */
		
		// Create the sudoku template generated before
		SudokuBuilder sub = new SudokuBuilder(sudoku.getSudokuType());
		for(Position p : getPositions(sudoku))
		{
			sub.addSolution(p, solution.get(p));//fill in all solutions
		}
		return sub.createSudoku();
	}
	
	private void createAllocation(Sudoku pattern)
	{
		//ensure all fields are defined
		while(!this.freeCells.isEmpty())
		{
			this.definedCells.add(this.freeCells.remove(0));
		}
		
		// Fill the sudoku being generated with template solutions
		//TODO simplify: iterate over fields/positions
		for(Position pos : getPositions(sudoku))
		{
			Cell fSudoku = sudoku.getCell(pos);
			Cell fSolved = pattern.getCell(pos);
			fSudoku.setCurrentValue(fSolved.getSolution(), false);
		}
		
		int reallocationAmount = 2; //getReallocationAmount(sudoku.getSudokuType(), 0.05);
		
		int plusminuscounter = 0;
		for(ComplexityRelation rel = INVALID;
			rel != CONSTRAINT_SATURATION;
			plusminuscounter++
		)
		{
			//every 1000 steps choose another random subset
			if(plusminuscounter % 1000 == 0 && plusminuscounter > 0)
			{
				//while (!this.freeFields.isEmpty()) {
				//	this.definedFields.add(this.freeFields.remove(0));
				//}
				
				removeDefinedCells(definedCells.size());
				for(int i = 0; i < cellsToDefine; i++)
				{
					addDefinedCell2();
				}
			}
			
			sudoku = removeAmbiguity(sudoku);
			
			//System.out.println(sudoku);

			/*solver = new Solver(sudoku);
			System.out.println("validate:");

			rel = solver.validate(null);
			System.out.println("validate says: " + rel);*/
			
			//fast validation where after 10 branchpoints we return too diificult
			
			FastBranchAndBound solver = new FastBranchAndBound(sudoku);
			rel = solver.validate();
			
			//System.out.println("Generator.run +/- loop. validate says " + rel);
			switch(rel)
			{
				case MUCH_TOO_EASY:
					removeDefinedCells(reallocationAmount);
					break;
				case TOO_EASY:
					removeDefinedCells(1);
					break;
				case INVALID:  //freeFields ARE empty ?! hence infinite loop
				case TOO_DIFFICULT:
				case MUCH_TOO_DIFFICULT:
					for(int i = 0; i < Math.min(reallocationAmount, freeCells.size()); i++)
					{
						addDefinedCell2();
					}
					break;
			}
			//System.out.println(" #definedFields: " + definedFields.size());
		}
	}
	
	/*
	 * While there are 2 solutions, add solution that is different in second sudoku
	 * Careful! If looking for the 2nd solution takes longer than x min, sudoku is declared unambiguous
	 */
	private Sudoku removeAmbiguity(Sudoku sudoku)
	{
		FastSolver fs = FastSolverFactory.getSolver(sudoku);
		//samurai take a long time -> try without uniqueness constraint
		//if (sudoku.getSudokuType().getEnumType() != SudokuTypes.samurai)
		while(fs.isAmbiguous())
		{
			Position p = fs.getAmbiguousPos();
			addDefinedCell2(p);
			fs = FastSolverFactory.getSolver(sudoku);
		}
		return sudoku;
	}
	
	private String reduceStringList(List<String> sl)
	{
		if(sl.size() == 0)
		{
			return "[]";
		}
		else if(sl.size() == 1)
		{
			return "[" + sl.get(0) + "]";
		}
		
		String s = "";
		Iterator<String> i = sl.iterator();
		int counter = 1;
		String last = i.next();
		while(i.hasNext())
		{
			String current = i.next();
			if(last.equals(current))
			{
				counter++;
			}
			else
			{
				s += (", " + counter) + '*' + last;
				last = current;
				counter = 0;
			}
		}
		s += (", " + counter) + '*' + last;
		
		return '[' + s.substring(2) + ']';
	}
	
	private List<String> gettypes(List<SolveDerivation> dl)
	{
		List<String> sl = new Stack<>();
		for(SolveDerivation sd : dl)
		{
			sl.add(sd.getType().toString());
			//sl.add(sd.toString());
		}
		return sl;
	}
	
	
	// Calculate the number of fields to be filled
	// the number is determined as the smaller of
	//        - the standard allocation factor defined in the type
	//        - the average #fields per difficulty level defined in the type
	private int getNumberOfCellsToDefine(SudokuType type, ComplexityConstraint desiredComplexityConstraint)
	{
		//TODO What do we have the allocation factor for??? can't it always be expressed through avg-fields?
		float standardAllocationFactor = type.getStandardAllocationFactor();
		int cellsOnSudokuBoard = type.getSize().getX() * type.getSize().getY();
		int cellsByType = (int) (cellsOnSudokuBoard * standardAllocationFactor); //TODO wäre freeFields.size nicht passender?
		int cellsByComp = desiredComplexityConstraint.getAverageCells();
		return Math.min(cellsByType, cellsByComp);
	}
	
	/** returns `percentage` percent of the #positions in the type
	 * e.g. for standard 9x9 and 0.5 -> 40 */
	private int getReallocationAmount(SudokuType st, double percentage)
	{
		int numberOfPositions = 0;
		for(Position p : sudoku.getSudokuType().getValidPositions())
		{
			numberOfPositions++;
		}
		
		int reallocationAmount = (int) (numberOfPositions * percentage); //remove/delete up to 10% of board
		return Math.max(1, reallocationAmount); // at least 1
	}
	
	/**
	 * Definiert ein weiteres Feld, sodass weiterhin Constraint Saturation
	 * vorhanden ist. Die Position des definierten Feldes wird
	 * zurückgegeben. Kann keines gefunden werden, so wird null
	 * zurückgegeben.
	 *
	 * This method is to be used for initialization only, once a solution is found
	 * please use addDefinedField2, with just chooses a free field to define
	 * and assumes constraint saturation.
	 *
	 * @return Die Position des definierten Feldes oder null, falls keines
	 *         gefunden wurde
	 */
	private Position addDefinedCell()
	{
		//TODO not sure what they do
		int xSize = sudoku.getSudokuType().getSize().getX();
		int ySize = sudoku.getSudokuType().getSize().getY();
		
		// Ein Array von Markierungen zum Testen, welches Felder belegt werden können
		/*true means marked, i.e. already defined or not part of the game e.g. 0,10 for samurai
		 *false means can be added
		 */
		boolean[][] markings = new boolean[xSize][ySize]; //all false by default.
		
		//definierte Felder markieren
		for(Position p : this.definedCells)
		{
			markings[p.getX()][p.getY()] = true;
		}
		
		/* avoids infitite while loop*/
		int count = definedCells.size();
		
		//find random {@code Position} p
		Position p = null;
		
		while(p == null && count < xSize * ySize)
		{
			int x = random.nextInt(xSize);
			int y = random.nextInt(ySize);
			if(sudoku.getCell(Position.get(x, y)) == null)
			{
				//position existiert nicht
				markings[x][y] = true;
				count++;
			}
			else if(markings[x][y] == false)
			{
				//pos existiert und ist unmarkiert
				p = Position.get(x, y);
			}
		}
		
		//construct a list of symbols starting at arbitrary point. there is no short way to do this without '%'
		int numSym = sudoku.getSudokuType().getNumberOfSymbols();
		int offset = random.nextInt(numSym);
		Queue<Integer> symbols = new LinkedList<>();
		for(int i = 0; i < numSym; i++)
		{
			symbols.add(i);
		}
		
		for(int i = 0; i < offset; i++)//rotate offset times
		{
			symbols.add(symbols.poll());
		}
		
		//constraint-saturierende belegung suchen
		boolean valid = false;
		for(int s : symbols)
		{
			sudoku.getCell(p).setCurrentValue(s, false);
			//alle constraints saturiert?
			valid = true;
			for(Constraint c : this.sudoku.getSudokuType())
			{
				if(!c.isSaturated(sudoku))
				{
					valid = false;
					sudoku.getCell(p).setCurrentValue(Cell.EMPTYVAL, false);
					break;
				}
			}
			if(valid)
			{
				definedCells.add(p);
				freeCells.remove(p); //if it's defined it is no longer free
				break;
			}
		}
		if(!valid)
		{
			p = null;
		}
		
		return p;
	}
	
	private void addDefinedCell2(int i)
	{
		Position p = freeCells.remove(i); //used to be 0, random just in case
		Cell fSudoku = sudoku.getCell(p);
		Cell fSolved = solvedSudoku.getCell(p);
		fSudoku.setCurrentValue(fSolved.getSolution(), false);
		definedCells.add(p);
	}
	
	private void addDefinedCell2(Position p)
	{
		int i = freeCells.indexOf(p);
		if(i < 0)
		{
			throw new IllegalArgumentException("position is not free, so it cannot be defined.");
		}
		addDefinedCell2(i);
	}
	
	
	/**
	 * choses a random free field and sets it as defined
	 */
	private void addDefinedCell2()
	{
		addDefinedCell2(random.nextInt(freeCells.size()));
	}
	
	/**
	 * Removes one of the defined fields (random selection)
	 *
	 * @return position of removed field or null is nothing there to remove
	 */
	private Position removeDefinedCell()
	{
		if(definedCells.isEmpty())
		{
			return null;
		}
		
		int nr = random.nextInt(definedCells.size());
		Position p = definedCells.remove(nr);
		sudoku.getCell(p).setCurrentValue(Cell.EMPTYVAL, false);
		freeCells.add(p);
		return p;
	}
	
	/**
	 * Tries {@code numberOfFieldsToRemove} times to remove a defined field
	 * @param numberOfCellsToRemove number of fields to remove
	 * @return list of removed positions
	 * */
	private List<Position> removeDefinedCells(int numberOfCellsToRemove)
	{
		ArrayList<Position> removed = new ArrayList<>();
		for(int i = 0; i < numberOfCellsToRemove; i++)
		{
			Position p = removeDefinedCell();
			if(p != null)
			{
				removed.add(p);
			}
		}
		
		return removed;
	}
	
	
	/**
	 * returns all positions of non-null Fields of sudoku
	 * @param sudoku a sudoku object
	 *
	 * @return list of positions whose corresponding {@code Field} objects are not null
	 */
	public static List<Position> getPositions(Sudoku sudoku)
	{
		List<Position> p = new ArrayList<>();
		for(int x = 0; x < sudoku.getSudokuType().getSize().getX(); x++)
		{
			for(int y = 0; y < sudoku.getSudokuType().getSize().getY(); y++)
			{
				if(sudoku.getCell(Position.get(x, y)) != null)
				{
					p.add(Position.get(x, y));
				}
			}
		}
		return p;
	}
	
	/* debugging, remove when done */
	public void printDebugMsg()
	{
		System.out.println("This is the debug message from `Generator`");
	}
	
	public void saveSudokuAllInOne(final String path, final String filename, Sudoku sudoku)
	{
		File sudokuLocation = FileManager.getSudokuDir();
		FileManager.initialize(FileManager.getProfilesDir(), new File(path));
		(
				new SudokuXmlHandler()
				{
					@Override
					protected File getFileFor(Sudoku s)
					{
						return new File(path + File.separator + filename);
					}
					
					@Override
					protected void modifySaveTree(XmlTree tree)
					{
						tree.addAttribute(new XmlAttribute("id", "42"));
					}
				}
		).saveAsXml(sudoku);
	}
	
	public static Sudoku getSudoku(String path, SudokuTypes st)
	{
		FileManager.initialize(
				new File("/home/t/Code/SudoQ/DebugOnPC/profilefiles"),
				new File("/home/t/Code/SudoQ/sudoq-app/sudoqapp/src/main/assets/sudokus/"));
		java.io.File f = new java.io.File(path);
		
		Sudoku s = new Sudoku(SudokuType.getSudokuType(st));
		try
		{
			s.fillFromXml(new XmlHelper().loadXml(f));
			s.setComplexity(Complexity.arbitrary);//justincase
			return s;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}

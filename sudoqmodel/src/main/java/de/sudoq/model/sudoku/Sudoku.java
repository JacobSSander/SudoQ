/*
 * SudoQ is a Sudoku-App for Adroid Devices with Version 2.2 at least.
 * Copyright (C) 2012  Heiko Klare, Julian Geppert, Jan-Bernhard Kordaß, Jonathan Kieling, Tim Zeitz, Timo Abele
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.sudoq.model.sudoku;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.sudoq.model.ModelChangeListener;
import de.sudoq.model.ObservableModelImpl;
import de.sudoq.model.sudoku.complexity.Complexity;
import de.sudoq.model.sudoku.sudokuTypes.SudokuType;
import de.sudoq.model.sudoku.sudokuTypes.SudokuTypes;
import de.sudoq.model.xml.XmlAttribute;
import de.sudoq.model.xml.XmlTree;
import de.sudoq.model.xml.Xmlable;

/**
 * Diese Klasse repräsentiert ein Sudoku mit seinem Typ, seinen Feldern und seinem Schwierigkeitsgrad.
 */
public class Sudoku extends ObservableModelImpl<Cell> implements Iterable<Cell>, Xmlable, ModelChangeListener<Cell>
{
	/* Attributes */
	
	/**
	 * Eine Identifikationsnummer, die ein Sudoku eindeutig identifiziert
	 */
	private int id;
	
	private int transformCount = 0;
	
	/**
	 * Eine Map, welche jeder Position des Sudokus ein Feld zuweist
	 */
	protected HashMap<Position, Cell> cells;
	
	private int cellIdCounter;
	private Map<Integer, Position> cellPositions;

	/**
	 * A map containing the symbols and number of occurrences
	 */

	private Map<Integer,Integer> symbolOccurrence;
	
	/**
	 * Der Typ dieses Sudokus
	 */
	private SudokuType type;
	
	/**
	 * Der Schwierigkeitsgrad dieses Sudokus
	 */
	private Complexity complexity;
	
	/* Constructors */
	
	/**
	 * Instanziiert ein Sudoku-Objekt mit dem spezifizierten SudokuType. Ist dieser null, so wird eine
	 * IllegalArgumentException geworfen. Alle Felder werden als editierbar ohne vorgegebene Lösung gesetzt.
	 *
	 * @param type
	 *            Der Typ des zu erstellenden Sudokus
	 * @throws IllegalArgumentException
	 *             Wird geworfen, falls der übergebene Typ null ist
	 */
	public Sudoku(SudokuType type)
	{
		//TODO warum so kompliziert? wenn type == null fliegt eh eine exception
		this(type, new PositionMap<Integer>(type == null ? Position.get(1, 1) : type.getSize()),
				new PositionMap<Boolean>(type == null ? Position.get(1, 1) : type.getSize()));
	}
	
	/**
	 * Instanziiert ein Sudoku-Objekt mit dem spezifizierten SudokuType. Ist dieser null, so wird eine
	 * IllegalArgumentException geworfen.
	 *
	 * @param type
	 *            Der Typ des zu erstellenden Sudokus
	 * @param map
	 *            Eine Map von Positions auf Lösungswerte. Werte in vorgegebenen Feldern sind verneint. (nicht negiert,
	 *            sondern bitweise verneint)
	 * @param setValues
	 *            Eine Map wo jede Position mit dem Wert true einen vorgegebenen Wert markiert
	 * @throws IllegalArgumentException
	 *             Wird geworfen, falls der übergebene Typ null ist
	 */
	public Sudoku(SudokuType type, PositionMap<Integer> map, PositionMap<Boolean> setValues)
	{
		if(type == null)
		{
			throw new IllegalArgumentException();
		}
		
		cellIdCounter = 1;
		cellPositions = new HashMap<>();
		
		this.type = type;
		this.cells = new HashMap<>();
		this.complexity = null;

		this.symbolOccurrence = new HashMap<>(type.getNumberOfSymbols());
		
		// iterate over the constraints of the type and create the fields
		for(Constraint constraint : type)
		{
			for(Position position : constraint)
			{
				if(!cells.containsKey(position))
				{
					Cell f;
					Integer solution = map == null ? null : map.get(position);
					if(solution != null)
					{
						boolean editable = setValues == null ||
								setValues.get(position) == null ||
								setValues.get(position) == false;
						f = new Cell(editable, solution, cellIdCounter, type.getNumberOfSymbols());
					}
					else
					{
						f = new Cell(cellIdCounter, type.getNumberOfSymbols());
					}
					
					cells.put(position, f);
					cellPositions.put(cellIdCounter++, position);
					f.registerListener(this);
				}
			}
		}

		//preload map with zeros
		for (int symbol: type.getSymbolIterator()) {
			symbolOccurrence.put(symbol, 0);
		}

		for (Cell cell:cells.values()) {
			if(cell.isSolved()){
				incrementSymbolOccurrence(cell.getCurrentValue());
			}
		}
	}
	
	/**
	 * Erzeugt ein vollständig leeres Sudoku, welches noch gefüllt werden muss. DO NOT USE THIS METHOD (if you are not
	 * from us)
	 */
	Sudoku()
	{
		id = -1;
	}
	
	/* Methods */
	
	/**
	 * Gibt die id dieses Sudokus zurueck
	 *
	 * @return die id
	 */
	public int getId()
	{
		return id;
	}
	
	/**
	 * Gibt an wie oft dieses Sudoku bereits transformiert wurde
	 *
	 * @return die anzahl der Transformationen
	 */
	public int getTransformCount()
	{
		return transformCount;
	}
	
	/**
	 * Zaehlt den transform Counter um 1 hoch
	 */
	public void increaseTransformCount()
	{
		transformCount++;
	}
	
	/**
	 * Gibt das Feld, welches sich an der spezifizierten Position befindet zurück. Ist position null oder in diesem
	 * Sudoku unbelegt, so wird null zurückgegeben.
	 *
	 * @param position Die Position, dessen Feld abgefragt werden soll
	 * @return Das Feld an der spezifizierten Position oder null, falls dies nicht existiert oder null übergeben wurde
	 */
	public Cell getCell(Position position)
	{
		return (position == null) ? null : cells.get(position);
	}
	
	/**
	 * Belegt die spezifizierte Position mit einem neuen Field.
	 * Falls field oder position null sind, bricht die Methode ab
	 *
	 * @param cell das neue Field
	 * @param position die Position des neuen Fields
	 */
	public void setCell(Cell cell, Position position)
	{
		if(cell == null || position == null)
		{
			return;
		}
		cells.put(position, cell);
		cellPositions.put(cell.getId(), position);
	}
	
	/**
	 * Gibt das Feld, das die gegebene id hat zurück. Ist id noch nicht vergeben wird null zurückgegeben
	 *
	 * @param id Die id des Feldes das ausgegeben werden soll
	 * @return Das Feld an der spezifizierten Position oder null, falls dies nicht existiert oder die id ungültig war
	 */
	public Cell getCell(int id)
	{
		return getCell(cellPositions.get(id));
	}
	
	/**
	 * Gibt die Position des Feldes, das die gegebene id hat zurück. Ist id noch nicht vergeben wird null zurückgegeben
	 *
	 * @param id Die id des Feldes der Position die ausgegeben werden soll
	 * @return Die spezifizierte Position oder null, falls diese nicht existiert oder die id ungültig war
	 */
	public Position getPosition(int id)
	{
		return cellPositions.get(id);
	}
	
	/**
	 * Gibt einen Iterator zurück, mithilfe dessen über alle Felder dieses Sudokus iteriert werden kann.
	 *
	 * @return Ein Iterator mit dem über alle Felder dieses Sudokus iteriert werden kann
	 */
	@Override
	public Iterator<Cell> iterator()
	{
		return cells.values().iterator();
	}
	
	/**
	 * Gibt den Schwierigkeitsgrad dieses Sudokus zurück.
	 *
	 * @return Der Schwierigkeitsgrad dieses Sudokus
	 */
	public Complexity getComplexity()
	{
		return complexity;
	}
	
	/**
	 * Gibt den Typ dieses Sudokus zurück.
	 *
	 * @return Der Typ dieses Sudokus
	 */
	public SudokuType getSudokuType()
	{
		return type;
	}
	
	/**
	 * Setzt den Schwierigkeitsgrad dieses Sudokus auf den Spezifizierten. Ist dieser ungültig so wird nichts getan.
	 *
	 * @param complexity Der Schwierigkeitsgrad auf den dieses Sudoku gesetzt werden soll
	 */
	public void setComplexity(Complexity complexity)
	{
		this.complexity = complexity;
	}
	
	/**
	 * Gibt an, ob das Sudoku vollstaendig ausgefuellt und korrekt geloest ist.
	 *
	 * @return true, falls das Sudoku ausgefüllt und gelöst ist, sonst false
	 */
	public boolean isFinished()
	{
		boolean allCorrect = true;
		for(Cell cell : cells.values())
		{
			allCorrect &= cell.isSolvedCorrect();
		}
		return allCorrect;
	}
	
	@Override
	public XmlTree toXmlTree()
	{
		XmlTree representation = new XmlTree("sudoku");
		if(id > 0)
		{
			representation.addAttribute(new XmlAttribute("id", "" + id));
		}
		representation.addAttribute(new XmlAttribute("transformCount", "" + transformCount));
		representation.addAttribute(new XmlAttribute("type", "" + this.getSudokuType().getEnumType().ordinal()));
		if(complexity != null)
		{
			representation.addAttribute(new XmlAttribute("complexity", "" + this.getComplexity().ordinal()));
		}
		
		for(Map.Entry<Position, Cell> field : cells.entrySet())
		{
			if(field.getValue() != null)
			{
				XmlTree fieldmap = new XmlTree("fieldmap");
				fieldmap.addAttribute(new XmlAttribute("id", "" + field.getValue().getId()));
				fieldmap.addAttribute(new XmlAttribute("editable", "" + field.getValue().isEditable()));
				fieldmap.addAttribute(new XmlAttribute("solution", "" + field.getValue().getSolution()));
				XmlTree position = new XmlTree("position");
				position.addAttribute(new XmlAttribute("x", "" + field.getKey().getX()));
				position.addAttribute(new XmlAttribute("y", "" + field.getKey().getY()));
				fieldmap.addChild(position);
				representation.addChild(fieldmap);
			}
		}
		
		return representation;
	}
	
	@Override
	public void fillFromXml(XmlTree xmlTreeRepresentation)
	{
		// initialisation
		cellIdCounter = 1;
		cellPositions = new HashMap<>();
		cells = new HashMap<>();
		symbolOccurrence = new HashMap<>();
		
		try
		{
			id = Integer.parseInt(xmlTreeRepresentation.getAttributeValue("id"));
		}
		catch(NumberFormatException e)
		{
			id = -1;
		}
		SudokuTypes enumType = SudokuTypes.values()[Integer.parseInt(xmlTreeRepresentation.getAttributeValue("type"))];
		type = SudokuBuilder.createType(enumType);
		transformCount = Integer.parseInt(xmlTreeRepresentation.getAttributeValue("transformCount"));
		
		String compl = xmlTreeRepresentation.getAttributeValue("complexity");
		complexity = compl == null ? null : Complexity.values()[Integer.parseInt(compl)];
		
		// build the fields
		for(XmlTree sub : xmlTreeRepresentation)
		{
			if(sub.getName().equals("fieldmap"))
			{
				int fieldId = Integer.parseInt(sub.getAttributeValue("id"));
				boolean editable = Boolean.parseBoolean(sub.getAttributeValue("editable"));
				int solution = Integer.parseInt(sub.getAttributeValue("solution"));
				int x = -1, y = -1;
				// check if there is only one child element
				if(sub.getNumberOfChildren() != 1)
				{
					throw new IllegalArgumentException();
				}
				XmlTree position = sub.getChildren().next();
				if(position.getName().equals("position"))
				{
					x = Integer.parseInt(position.getAttributeValue("x"));
					y = Integer.parseInt(position.getAttributeValue("y"));
				}
				Position pos = Position.get(x, y);
				Cell cell = new Cell(editable, solution, fieldId, type.getNumberOfSymbols());
				cell.registerListener(this);
				cells.put(pos, cell);
				cellPositions.put(fieldId, pos);
				cellIdCounter++;
			}
		}

		//preload map with zeros
		for (int symbol: type.getSymbolIterator()) {
			symbolOccurrence.put(symbol, 0);
		}

		for (Cell cell:cells.values()) {
			if(cell.isSolved()){
				incrementSymbolOccurrence(cell.getCurrentValue());
			}
		}
	}
	
	@Override
	public void onModelChanged(Cell changedCell)
	{
		notifyListeners(changedCell);
	}
	
	/**
	 * Setzt die Identifikationsnummer des Sudokus.
	 *
	 * @param id Die eindeutige Identifikationsnummer
	 */
	public void setId(int id)
	{
		this.id = id;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj != null && obj instanceof Sudoku)
		{
			Sudoku other = (Sudoku) obj;
			
			boolean complexityMatch = this.complexity == other.getComplexity();
			boolean typeMatch = type.getEnumType() == other.getSudokuType().getEnumType();
			
			boolean fieldsMatch = true;
			for(Cell f : this.cells.values())
			{
				fieldsMatch &= f.equals(other.getCell(f.getId()));
			}
			
			return complexityMatch && typeMatch && fieldsMatch;
		}
		return false;
	}
	
	/**
	 * Gibt zurück, ob dieses Sudoku in den aktuell gesetzten Werten Fehler enthält, d.h. ob es ein Feld gibt, dessen
	 * aktueller Wert nicht der korrekten Lösung entspricht.
	 *
	 * @return true, falls es in dem Sudoku falsch gelöste Felder gibt, false andernfalls
	 */
	public boolean hasErrors()
	{
		for(Cell f : this.cells.values())
		{
			if(!f.isNotWrong())
			{
				return true;
			}
		}
		return false;
		//return this.fields.values().stream().anyMatch(f -> !f.isNotWrong()); //looks weird but be very careful with simplifications!
	}

	/**
	 * Increments the occurrence counter for the given symbol by one
	 *
	 * @param symbol The symbol for which the counter should be incremented
	 */
	public void incrementSymbolOccurrence(int symbol){
		if(symbolOccurrence.containsKey(symbol)){
			int count = symbolOccurrence.get(symbol);
			count = count < type.getNumberOfSymbols() ? count + 1 : type.getNumberOfSymbols();
			symbolOccurrence.put(symbol, count);
		}
	}

	/**
	 * Decrements the occurrence counter for the given symbol by one
	 *
	 * @param symbol The symbol for which the counter should be decremented
	 */
	public void decrementSymbolOccurrence(int symbol){
		if(symbolOccurrence.containsKey(symbol)){
			int count = symbolOccurrence.get(symbol);
			count = count > 0 ? count - 1 : 0;
			symbolOccurrence.put(symbol, count);
		}
	}

	public Map<Integer,Integer> getSymbolOccurrence(){
		return new HashMap<>(symbolOccurrence);
	}
	
	//debug
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		final String OFFSET = type.getNumberOfSymbols() < 10 ? "" : " ";
		final String EMPTY = type.getNumberOfSymbols() < 10 ? "x" : "xx";
		final String NONE = type.getNumberOfSymbols() < 10 ? " " : "  ";
		for(int j = 0; j < getSudokuType().getSize().getY(); j++)
		{
			for(int i = 0; i < getSudokuType().getSize().getX(); i++)
			{
				Cell f = getCell(Position.get(i, j));
				String op;
				if(f != null)
				{
					//feld existiert
					int value = f.getCurrentValue();
					if(value == -1)
					{
						op = EMPTY;
					}
					else if(value < 10)
					{
						op = OFFSET + value;
					}
					else
					{
						op = value + "";
					}
					sb.append(op);
				}
				else
				{
					sb.append(NONE);
				}
				sb.append(" ");//separator
			}
			sb.replace(sb.length() - 1, sb.length(), "\n");
		}
		sb.delete(sb.length() - 1, sb.length());
		return sb.toString();
	}
	
	/**
	 * creates a perfect clone,
	 */
	@Override
	public Object clone()
	{
		Sudoku clone = new Sudoku(this.type);
		clone.id = this.id;
		clone.transformCount = this.transformCount;
		clone.cells = new HashMap<>();
		for(Map.Entry<Position, Cell> e : this.cells.entrySet())
		{
			clone.cells.put(e.getKey(), (Cell) e.getValue().clone());
		}
		clone.cellIdCounter = this.cellIdCounter;
		clone.cellPositions = new HashMap<>(this.cellPositions);
		clone.complexity = this.complexity;
		return clone;
	}
}

package de.sudoq.view.hints;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import java.util.List;
import java.util.Stack;

import de.sudoq.model.solverGenerator.solution.SolveDerivation;
import de.sudoq.view.sudoku.SudokuLayout;

public class HintView extends View
{
	SudokuLayout sl;
	SolveDerivation derivation;
	
	List<View> highlightedObjects;
	
	public HintView(Context context, SudokuLayout sl, SolveDerivation d)
	{
		super(context);
		if(context == null)
		{
			throw new IllegalArgumentException();
		}
		
		this.sl = sl;
		this.derivation = d;
		highlightedObjects = new Stack<>();
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		for(View v : highlightedObjects)
		{
			v.draw(canvas);
		}
	}
}

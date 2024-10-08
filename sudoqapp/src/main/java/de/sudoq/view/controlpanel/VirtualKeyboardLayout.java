/*
 * SudoQ is a Sudoku-App for Adroid Devices with Version 2.2 at least.
 * Copyright (C) 2012  Heiko Klare, Julian Geppert, Jan-Bernhard Kordaß, Jonathan Kieling, Tim Zeitz, Timo Abele
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.sudoq.view.controlpanel;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Iterator;

import de.sudoq.controller.sudoku.board.CellViewPainter;
import de.sudoq.controller.sudoku.board.CellViewStates;
import de.sudoq.controller.sudoku.InputListener;
import de.sudoq.controller.sudoku.ObservableInput;

/**
 * Dieses Layout stellt ein virtuelles Keyboard zur Verfügung, in dem sich die
 * Buttons möglichst quadratisch ausrichten.
 */
public class VirtualKeyboardLayout extends LinearLayout implements ObservableInput, Iterable<View>
{
	/**
	 * Die Buttons des VirtualKeyboard
	 */
	private VirtualKeyboardButtonView[][] buttons;
	
	private Iterable<VirtualKeyboardButtonView> buttonIterator = new Iterable<VirtualKeyboardButtonView>()
	{
		@Override
		public Iterator<VirtualKeyboardButtonView> iterator()
		{
			return new Iterator<VirtualKeyboardButtonView>()
			{
				int i = 0;
				int j = 0;
				
				@Override
				public boolean hasNext()
				{
					return i < buttons.length;
				}
				
				@Override
				public VirtualKeyboardButtonView next()
				{
					VirtualKeyboardButtonView current = buttons[i][j++];
					if(j == buttons[i].length)
					{
						j = 0;
						i++;
					}
					return current;
				}
				
				@Override
				public void remove()
				{
				}
			};
		}
	};
	
	/**
	 * Beschreibt, ob die Tastatur deaktiviert ist.
	 */
	private boolean deactivated;
	
	/**
	 * Instanziiert ein neues VirtualKeyboardLayout mit den gegebenen Parametern
	 *
	 * @param context der Applikationskontext
	 * @param attrs das Android AttributeSet
	 */
	public VirtualKeyboardLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.setWillNotDraw(false);
	}
	
	/**
	 * Aktualisiert das Keyboard, sodass für das angegebene Game die korrekten
	 * Buttons dargestellt werden.
	 *
	 * @param numberOfButtons Die Anzahl der Buttons für dieses Keyboard
	 */
	public void refresh(int numberOfButtons)
	{
		if(numberOfButtons < 0)
		{
			return;
		}
		this.deactivated = false;
		inflate(numberOfButtons);
	}
	
	/**
	 * Inflatet das Keyboard.
	 *
	 * @param numberOfButtons Anzahl der Buttons dieser Tastatur
	 */
	private void inflate(int numberOfButtons)
	{
		this.removeAllViews();
		int buttonsPerColumn = (int) Math.floor(Math.sqrt(numberOfButtons));
		int buttonsPerRow = (int) Math.ceil(Math.sqrt(numberOfButtons));
		this.buttons = new VirtualKeyboardButtonView[buttonsPerRow][buttonsPerColumn];
		
		for(int y = 0; y < buttonsPerColumn; y++)
		{
			LinearLayout la = new LinearLayout(getContext());
			for(int x = 0; x < buttonsPerRow; x++)
			{
				this.buttons[x][y] = new VirtualKeyboardButtonView(getContext(), x + y * buttonsPerRow);
				this.buttons[x][y].setVisibility(View.INVISIBLE);
				LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f);
				params.leftMargin = 2;
				params.bottomMargin = 2;
				params.topMargin = 2;
				params.rightMargin = 2;
				la.addView(this.buttons[x][y], params);
			}
			addView(la, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f));
		}
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
//		FieldViewPainter.getInstance().markField(canvas, this, ' ', false);
	}
	
	/**
	 * Aktiviert bzw. deaktiviert dieses Keyboard.
	 *
	 * @param activated Spezifiziert, ob das Keyboard aktiviert oder deaktiviert sein soll
	 */
	public void setActivated(boolean activated)
	{
		for(VirtualKeyboardButtonView b : buttonIterator)
		{
			b.setVisibility(activated ? View.VISIBLE
					: View.INVISIBLE);
		}
	}
	
	/**
	 * Unbenutzt.
	 *
	 * @throws UnsupportedOperationException Wirft immer eine UnsupportedOperationException
	 */
	public void notifyListeners()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void registerListener(InputListener listener)
	{
		for(VirtualKeyboardButtonView b : buttonIterator)
		{
			b.registerListener(listener);
		}
	}
	
	@Override
	public void removeListener(InputListener listener)
	{
		for(VirtualKeyboardButtonView b : buttonIterator)
		{
			b.removeListener(listener);
		}
	}
	
	/**
	 * Markiert das spezifizierte Feld mit dem übergebenen Status, um von dem
	 * FieldViewPainter entsprechend gezeichnet zu werden.
	 *
	 * @param symbol Das Symbol des Feldes
	 * @param state Der zu setzende Status
	 */
	public void markCell(int symbol, CellViewStates state)
	{
		int buttonsPerRow = this.buttons.length;
		CellViewPainter.getInstance().setMarking(this.buttons[symbol % buttonsPerRow][symbol / buttonsPerRow], state);
		this.buttons[symbol % buttonsPerRow][symbol / buttonsPerRow].invalidate();
	}
	
	/**
	 * Aktiviert alle Buttons dieses Keyboards.
	 */
	public void enableAllButtons()
	{
		for(VirtualKeyboardButtonView b : buttonIterator)
		{
			b.setEnabled(true);
		}
	}
	
	/**
	 * Deaktiviert den spezifizierten Button.
	 *
	 * @param symbol Das Symbol des zu deaktivierenden Button
	 */
	public void disableButton(int symbol)
	{
		int buttonsPerRow = this.buttons.length;
		this.buttons[symbol % buttonsPerRow][symbol / buttonsPerRow].setEnabled(false);
	}
	
	@Override
	public void invalidate()
	{
		if(this.buttons == null)
		{
			return;
		}
		
		for(VirtualKeyboardButtonView b : buttonIterator)
		{
			if(b != null)
			{
				b.invalidate();
			}
		}
	}
	
	/**
	 * Gibt zurueck ob die view angezeigt wird
	 *
	 * @return true falls aktive andernfalls false
	 */
	public boolean isActivated()
	{
		return !deactivated;
	}
	
	@Override
	public Iterator<View> iterator()
	{
		return new Iterator<View>()
		{
			int i = 0;
			
			@Override
			public boolean hasNext()
			{
				return i < getChildCount();
			}
			
			@Override
			public View next()
			{
				return getChildAt(i++);
			}
			
			@Override
			public void remove()
			{
			}
		};
	}
}

/*
 * SudoQ is a Sudoku-App for Adroid Devices with Version 2.2 at least.
 * Copyright (C) 2012  Heiko Klare, Julian Geppert, Jan-Bernhard Kordaß, Jonathan Kieling, Tim Zeitz, Timo Abele
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.sudoq.activities.menus.preferences;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import de.sudoq.R;
import de.sudoq.controller.menu.Utility;
import de.sudoq.model.sudoku.sudokuTypes.SudokuTypes;
import de.sudoq.model.xml.SudokuTypesList;

/**
 * Adapter für die Anzeige aller zu wählenden Sudoku Typen
 */
public class OfferedTypesAdapter extends ArrayAdapter<SudokuTypes>
{
	private static final String LOG_TAG = OfferedTypesAdapter.class.getSimpleName();
	private final Context context;
	private final List<SudokuTypes> types;
	
	/**
	 * Erzeugt einen neuen SudokuLoadingAdpater mit den gegebenen Parametern
	 *
	 * @param context der Applikationskontext
	 * @param typesList die Liste der Typen
	 */
	public OfferedTypesAdapter(Context context, SudokuTypesList typesList)
	{
		super(context, R.layout.restricttypes_item, typesList.getAllTypes());
		this.context = context;
		this.types = typesList;
		Log.d("rtAdap", "rtAdap is initialized, size: " + types.size());
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.restricttypes_item, parent, false);
		SudokuTypes type = super.getItem(position);
		String full = Utility.type2string(context, type);//translated name of Sudoku type;
		
		((View) rowView.findViewById(R.id.regular_languages_layout)).setVisibility(View.GONE);
		((View) rowView.findViewById(R.id.irregular_languages_layout)).setVisibility(View.VISIBLE);
		
		TextView sudokuType = (TextView) rowView.findViewById(R.id.combined_label);
		sudokuType.setText(full);
		
		int color = types.contains(type) ? Color.BLACK : Color.LTGRAY;
		sudokuType.setTextColor(color);
		
		return rowView;
	}
}

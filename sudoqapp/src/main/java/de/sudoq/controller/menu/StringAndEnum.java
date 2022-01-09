package de.sudoq.controller.menu;

import androidx.annotation.NonNull;

//Note/Ecconia: Changed from protected to public, cause of package move.
public class StringAndEnum<E>
{
	private String s;
	private E e;
	
	public StringAndEnum(String s, E e)
	{
		this.s = s;
		this.e = e;
	}
	
	public String getString()
	{
		return s;
	}
	
	public E getEnum()
	{
		return e;
	}
	
	@NonNull
	@Override
	public String toString()
	{
		return s;
	}
}

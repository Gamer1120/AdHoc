package com.procoder.routing.client;

import java.util.*;

/**
 * Class for transmitting data to other nodes. Table format is used to make it
 * easier to transmit complex data structures.
 * 
 * @author Jaco
 * @version 08-03-2015
 */
public class DataTable {
	private List<List<Integer>> data = new ArrayList<List<Integer>>();
	private int columns;
	
	/**
	 * Gets the number of columns in this table
	 * @return number of columns
	 */
	public int getNColumns(){
		return this.columns;
	}
	
	/**
	 * Gets the number of rows in this table
	 * @return number of rows
	 */
	public int getNRows(){
		return this.data.size();
	}

	/**
	 * Construct a DataTable
	 * 
	 * @param nColumns
	 *            the number of columns in this table
	 */
	public DataTable(int nColumns){
		if (nColumns < 0){
			throw new IllegalArgumentException("nColumns must be >= 0");
		}
		this.columns = nColumns;
	}

	/**
	 * Set a table cell to a value. If the row does not exist, it is created and
	 * initialized to all 0's.
	 * 
	 * @param row Row index, starting at 0
	 * @param column Column index, starting at 0
	 * @param value
	 */
	public void set(int row, int column, int value) {
		if (column < 0 || column >= this.columns){
			throw new IllegalArgumentException("Column index (" + column + ") out of range (0.." + this.columns + ").");
		}
		
		if (row < 0){
			throw new IllegalArgumentException("Row index < 0");
		}
		
		while (this.data.size() <= row) {
			List<Integer> newRow = new ArrayList<Integer>();
			for (int i = 0; i < this.columns; i++) {
				newRow.add(0);
			}
			this.data.add(newRow);
		}

		this.data.get(row).set(column, value);
	}
	
	/**
	 * Gets a value from the table
	 * @param row Row index, starting at 0
	 * @param column Column index, starting at 0
	 * @return value
	 */
	public int get(int row, int column){
		if (column < 0 || column >= this.columns){
			throw new IllegalArgumentException("Column index (" + column + ") out of range (0.." + this.columns + ").");
		}
		
		if (row < 0 || row >= this.data.size()){
			throw new IllegalArgumentException("Row index (" + row + ") out of range (0.." + this.data.size() + ").");
		}
		
		return this.data.get(row).get(column);
	}
	
	/**
	 * Inserts a row into the DataTable
	 * @param row Row index, starting at 0
	 * @param values Integer array of values
	 */
	public void setRow(int row, Integer[] values){
		if (values.length != this.columns){
			throw new IllegalArgumentException("values array size (" + values.length + ") must match DataTable nColumns (" + this.columns + ").");
		}
		
		for (int i=0; i<values.length; i++){
			this.set(row, i, values[i]);
		}
	}
	
	/**
	 * Retrieves a row from the DataTable
	 * @param row Row index, starting at 0
	 * @return Integer array of valus
	 */
	public Integer[] getRow(int row){
		if (row < 0 || row >= this.data.size()){
			throw new IllegalArgumentException("Row index (" + row + ") out of range (0.." + this.data.size() + ").");
		}
		
		Integer[] returnArray = new Integer[this.columns];
		for(int i=0; i<this.columns; i++){
			returnArray[i] = this.get(row, i);
		}
		return returnArray;
	}
	
	/**
	 * Adds a row to the end of the DataTable
	 * @param values Integer array of values
	 */
	public void addRow(Integer[] values){
		if (values.length != this.columns){
			throw new IllegalArgumentException("values array size (" + values.length + ") must match DataTable nColumns (" + this.columns + ").");
		}
		
		this.setRow(this.data.size(), values);
	}
}

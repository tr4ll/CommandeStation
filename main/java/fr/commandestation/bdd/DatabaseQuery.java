package fr.commandestation.bdd;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

/**
 * This class adds multiple entries to the database and pulls them back out.
 * 
 * @author Hanly De Los Santos (http://hdelossantos.com)
 */
public class DatabaseQuery {
	// Variables area
	private ArrayList<String> arrayKeys = null;
	private ArrayList<String> arrayValues = null;
	private ArrayList<String> databaseKeys = null;
	private ArrayList<String> databaseKeyOptions = null;
	private DBAdapter database;


	/**
	 * Initialize the ArrayList
	 * 
	 * @param context
	 *            Pass context from calling class.
	 */
	public DatabaseQuery(Context context, String name, ArrayList<String> keys) {
		// Create an ArrayList of keys and one of the options/parameters
		// for the keys.
		databaseKeys = new ArrayList<String>();
		databaseKeyOptions = new ArrayList<String>();
		databaseKeys.add("Title");
		databaseKeyOptions.add("text not null");

		// Call the database adapter to create the database
		database = new DBAdapter(context, name, keys, databaseKeyOptions);
		database.open();
		arrayKeys = new ArrayList<String>();
		arrayValues = new ArrayList<String>();

	}

	/**
	 * Append data to an ArrayList to then submit to the database
	 * 
	 * @param key
	 *            Key of the value being appended to the Array.
	 * @param value
	 *            Value to be appended to Array.
	 */
	public void appendData(String key, String value) {
		arrayKeys.add(key);
		arrayValues.add(value);
	}

	/**
	 * This method adds the row created by appending data to the database. The
	 * parameters constitute one row of data.
	 */
	public void addRow() {
		database.insertEntry(arrayKeys, arrayValues);
	}
	
	/**
	 * Permet de mettre a jour une ligne dans la BDD
	 * @param id 
	 * 			numero de la ligne
	 * @param champ
	 * 			List des champ a mettre a jour
	 * @param valeur
	 * 			List des valeur a mettre a jour
	 * 
	 */
	public void updateRow(String sqlQuery){
		
		database.update(sqlQuery);
	}
	/**
	 * Cette methode permet de supprim� une ligne de la bdd avec une cl� et une valeur
	 * (De pr�f�rence une cl� unique)
	 * @param key
	 *        Champ de la table
	 * @param value
	 *        Valeur de la cl�
	 */
	public void removeRow(String key,String value)
	{
		database.removeEntry(key,value);
	}
	/**
	 * Get data from the table.
	 * 
	 * @param keys
	 *            List of columns to include in the result.
	 * @param selection
	 *            Return rows with the following string only. Null returns all
	 *            rows.
	 * @param selectionArgs
	 *            Arguments of the selection.
	 * @param groupBy
	 *            Group results by.
	 * @param having
	 *            A filter declare which row groups to include in the cursor.
	 * @param sortBy
	 *            Column to sort elements by.
	 * @param sortOption
	 *            ASC for ascending, DESC for descending.
	 * @return Returns an ArrayList<String> with the results of the selected
	 *         field.
	 */
	public ArrayList<String> getData(String[] keys, String selection,
			String[] selectionArgs, String groupBy, String having,
			String sortBy, String sortOption) {

		ArrayList<String> list = new ArrayList<String>();
		Cursor results = database.getAllEntries(keys, selection, selectionArgs,
				groupBy, having, sortBy, sortOption);
		while (results.moveToNext())
			list.add(results.getString(results.getColumnIndex(sortBy)));
		return list;

	}
	public HashMap<String, String> getDataFields(String[] keys, String selection,
			String[] selectionArgs, String groupBy, String having,
			String sortBy, String sortOption) {
		int i = 0;
		HashMap<String,String> list = new HashMap<String,String>();
		Cursor results = database.getAllEntries(keys, selection, selectionArgs,
				groupBy, having, sortBy, sortOption);
		while (results.moveToNext())
			while ( i < keys.length)
			{
				list.put(keys[i], results.getString(results.getColumnIndex(keys[i])));
				i++;
			}
		return list;

	}
	/**
	 * Destroy the reporter.
	 */
	public void destroy() {
		try{
			database.close();
		}catch (SQLException e) {
			Log.e("DATABASE",e.toString());
		}
	}
}

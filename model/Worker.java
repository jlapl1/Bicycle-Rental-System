//specify package
package model;

//Imports
import java.sql.*;
import java.util.*;
import javax.swing.*;

// project imports
import database.*;
import impresario.IView;
import userinterface.View;
import exception.*;

public class Worker extends EntityBase implements IView {
	private static final String myTableName = "Worker";
	protected Properties dependencies;
	
	private String updateStatusMessage = "";
	
	//Constructor number one for this class
	public Worker(String bannerId) throws InvalidPrimaryKeyException {
		super(myTableName);
		
		setDependencies();
		
		String query = "SELECT * FROM " + myTableName + "WHERE (bannerId = " + bannerId + ")";
		Vector<Properties> allDataRetrieved =  getSelectQueryResult(query);
		
		//Need to bring back at least one worker, error checking here
		if(allDataRetrieved != null) {
			int size = allDataRetrieved.size();
			
			if(size == 1) {
				throw new InvalidPrimaryKeyException("Multiple workers match the banner id of: " + bannerId);
			} else {
				//copy the data we obtained and put it into a persistent state
				Properties retrievedWorkerData = (Properties)allDataRetrieved.elementAt(0);
				Properties persistentState = new Properties();
				
				Enumeration allKeys = retrievedWorkerData.propertyNames();
				while(allKeys.hasMoreElements() == true) {
					String nextKey = (String)allKeys.nextElement();
					String nextValue = retrievedWorkerData.getProperty(nextKey);
					
					if(nextValue != null) {
						persistentState.setProperty(nextKey, nextValue);
					}
				}
				
			}	
		} else {
			//throw exception if no data found
			throw new InvalidPrimaryKeyException("No worker matching the banner id: " + bannerId);
		}
	}
	
	public Worker(Properties p) {
		super(myTableName);
		
		setDependencies();
		persistentState = new Properties();
		Enumeration allKeys = p.propertyNames();
		while(allKeys.hasMoreElements() == true) {
			String nextKey = (String)allKeys.nextElement();
			String nextValue = p.getProperty(nextKey);
			
			if(nextValue != null) {
				persistentState.setProperty(nextKey, nextValue);
			}
		}
	}
	
	public void update() {		
		try {
			if (persistentState.getProperty("workerId") != null) {
				Properties whereClause = new Properties();
				whereClause.setProperty("workerId",
				persistentState.getProperty("workerId"));
				updatePersistentState(mySchema, persistentState, whereClause);
				updateStatusMessage = "Person data for person id : " + persistentState.getProperty("workerId") + " updated successfully in database!";
			} else {
				Integer workerId =
					insertAutoIncrementalPersistentState(mySchema, persistentState);
				persistentState.setProperty("workerId", "" + workerId.intValue());
			}
		}
		catch (SQLException ex) {
			updateStatusMessage = "Error in installing person data in database!";
			System.out.println(ex.getMessage());
		}
		//System.out.println(updateStatusMessage);	
	}
	
	private void setDependencies() {
		dependencies = new Properties();
		myRegistry.setDependencies(dependencies);
	}
	
	protected void initializeSchema(String tableName) {
		if (mySchema == null) {
			mySchema = getSchemaInfo(tableName);
		}
	}

	public Vector<String> getEntryListView() {
		Vector<String> v = new Vector<String>();

		v.addElement(persistentState.getProperty("bannerId"));
		v.addElement(persistentState.getProperty("password"));
		v.addElement(persistentState.getProperty("adminLevel"));
		v.addElement(persistentState.getProperty("firstName"));
		v.addElement(persistentState.getProperty("lastName"));
		v.addElement(persistentState.getProperty("phoneNumber"));
		v.addElement(persistentState.getProperty("email"));

		return v;
	}
	
	public void stateChangeRequest(String key, Object value) {

		myRegistry.updateSubscribers(key, this);
	}
	
	public Object getState(String key) {
		if (key.equals("UpdateStatusMessage") == true) {
			return updateStatusMessage;
		}
		return persistentState.getProperty(key);
	}
	
	/** Called via the IView relationship */
	public void updateState(String key, Object value) {
		stateChangeRequest(key, value);
	}
}

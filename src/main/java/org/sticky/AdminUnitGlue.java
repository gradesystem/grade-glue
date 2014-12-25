package org.sticky;

import static org.sticky.Glues.*;

import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;
import org.virtualrepository.csv.CsvAsset;
import org.virtualrepository.csv.CsvTable;
import org.virtualrepository.tabular.Table;


public class AdminUnitGlue {

	Table countries;
	
	Table flagstates;
	
	@Before
	public void before(){
		
		//countries
		CsvAsset asset1 = new CsvAsset("someid","gaul-codes");
		asset1.hasHeader(true);
		asset1.setDelimiter(',');
		Table codesTable = new CsvTable(asset1, load("gaul-codes.txt"));
		
		CsvAsset asset2 = new CsvAsset("someid","gaul-names");
		asset2.hasHeader(true);
		asset2.setDelimiter('\t');
		asset2.setEncoding(Charset.forName("UTF-16"));
		Table namesTable = new CsvTable(asset2, load("gaul-names.txt"));
		
		countries = buildGaulTable(codesTable, namesTable);
		
		//flagstates
		CsvAsset asset3 = new CsvAsset("someid","flagstates");
		asset3.hasHeader(true);
		asset3.setDelimiter(',');
		flagstates = new CsvTable(asset3, load("flagstates.txt"));
		
		countries = enrichGaulTable(countries, flagstates);
		
	}
	
	/**
	 * Grabs the GAUL codelist after joining codes and names
	 * and adding flagstate information
	 * 
	 */
	@Test
	public void grabAdminUnits(){
		
		//TODO grab countries table
		
	}
	
	/**
	 * Push the GAUL codelist after joining codes and names
	 * and adding flagstate information
	 * 
	 */
	@Test
	public void pushAdminUnits(){
		
		//TODO push countries table
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Joins GAUL codes with names (separated tables provided as CSV assets)
	 * 
	 * @return a VR Table
	 */
	static Table buildGaulTable(Table codes, Table names){

		//TODO a simple join between Table to get names in a single table?
		
		return(codes);
	}
	
	/**
	 * Enrichs the GAUL table with flagstate information
	 * 
	 * @returns a VR Table
	 * 
	 */
	static Table enrichGaulTable(Table countries, Table flagstates){
		
		//TODO enrich GAUL table with flagstates
		
		return(null);
	}
	
}
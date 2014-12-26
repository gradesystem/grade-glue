package org.sticky;

import static org.sticky.Glues.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;
import org.virtualrepository.csv.CsvAsset;
import org.virtualrepository.csv.CsvTable;
import org.virtualrepository.tabular.Column;
import org.virtualrepository.tabular.DefaultTable;
import org.virtualrepository.tabular.Row;
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
		asset3.setDelimiter(';');
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
		
		Glues.store("admin-units.txt", countries);
	}
	
	/**
	 * Push the GAUL codelist after joining codes and names
	 * and adding flagstate information
	 * 
	 */
	@Test
	public void pushAdminUnits(){
		
		//TODO push countries table as xml
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Joins GAUL codes with names (separated tables provided as CSV assets)
	 * (TO DO). At now the method does only return the 'codes' table.
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
		
		//extract flagstate list
		Set<String> fsCodes = new HashSet<String>();
		for(Row row : flagstates){
			String code = row.get("iso3code");
			if(!fsCodes.contains(code)){
				fsCodes.add(code);
			}
		}
		
		//enrich GAUL table with flagstates
		List<Column> columns = countries.columns();
		columns.add(new Column("isFlagstate"));
		
		List<Row> rows = new ArrayList<Row>();
		for(Row row : countries){
			
			Map<QName,String> data = new HashMap<QName,String>();
			for(Column col : countries.columns()){
				data.put(col.name(), row.get(col.name()));
			}
			String code = row.get("ISO3");
			boolean isFlagstate = false;
			if(fsCodes.contains(code)){
				isFlagstate = true;
			}
			data.put(new QName("isFlagstate"), String.valueOf(isFlagstate));
			
			Row newRow = new Row(data);
			rows.add(newRow);
		}
		
		Table output = new DefaultTable(columns, rows.iterator());
		return(output);
	}
	
	
}
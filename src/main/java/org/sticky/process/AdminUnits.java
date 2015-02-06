package org.sticky.process;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import lombok.SneakyThrows;

import org.virtualrepository.tabular.Column;
import org.virtualrepository.tabular.DefaultTable;
import org.virtualrepository.tabular.Row;
import org.virtualrepository.tabular.Table;


public class AdminUnits {

	
	////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Joins GAUL codes with names (separated tables provided as CSV assets)
	 * 
	 * @return a VR Table
	 */
	public static Table buildAdminUnitsTable(Table codes, Table names){
		
		//new columns
		List<Column> columns = codes.columns();
		for(Column col : names.columns()){
			columns.add(col);
		}
		
		//name row list
		List<Row> nameRows = new ArrayList<Row>();
		Iterator<Row> rit = names.iterator();
		while(rit.hasNext()){
			Row nameRow = rit.next();
			
			Map<QName,String> data = new HashMap<QName,String>();
			for(Column col : names.columns()){
				data.put(col.name(), nameRow.get(col));
			}
			
			Row newRow = new Row(data);
			nameRows.add(newRow);
		}
		
		//merge tables based on Gaul code
		List<Row> rows = new ArrayList<Row>();
		for(Row row : codes){
			
			Map<QName,String> data = new HashMap<QName,String>();
			for(Column col : codes.columns()){
				data.put(col.name(), row.get(col));
			}
			
			String code = row.get("code");
			Iterator<Row> rowit = nameRows.iterator();
			while(rowit.hasNext()){
				Row nameRow = rowit.next();
				if(nameRow.get("GAUL").equals(code)){
					for(Column col : names.columns()){
						data.put(col.name(), nameRow.get(col.name()));
					}
				}
			}
	
			Row newRow = new Row(data);
			rows.add(newRow);
		}
		
		Table output = new DefaultTable(columns, rows.iterator());
		
		return(output);
	}
	
	/**
	 * Enrichs the GAUL table with flagstate information
	 * 
	 * @returns a VR Table
	 * 
	 */
	public static Table enrichAdminUnitsTable(Table countries, Table flagstates){
		
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
	
	
	//////////////////////////////////////////////////// helper
	
	@SneakyThrows
	void $print(Source source) {
		
		 TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		StringWriter stringWriter = new StringWriter();
		 StreamResult streamResult = new StreamResult(stringWriter);
		transformer.transform(source, streamResult);
		System.out.println(stringWriter);
	}
	
}
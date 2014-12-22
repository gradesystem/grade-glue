package org.sticky;

import static org.sticky.Glues.*;

import java.nio.charset.Charset;

import org.junit.Test;
import org.virtualrepository.csv.CsvAsset;
import org.virtualrepository.csv.CsvTable;
import org.virtualrepository.tabular.Row;
import org.virtualrepository.tabular.Table;

public class GaulGlue {

	@Test
	public void readGaulCodes() {
		
		
		CsvAsset asset = new CsvAsset("someid","gaul-codes");
		
		asset.hasHeader(true);
		asset.setDelimiter(','); //it's the default, just to illustrate
		
		
		Table table = new CsvTable(asset, load("gaul-codes.txt"));
		
		for (Row row : table)
			System.out.println(row);
	}
	
	
	@Test
	public void readGaulNames() {
		
		
		CsvAsset asset = new CsvAsset("someid","gaul-names");
		
		asset.hasHeader(true);
		asset.setDelimiter('\t');
		asset.setEncoding(Charset.forName("UTF-16"));
		
		
		Table table = new CsvTable(asset, load("gaul-names.txt"));
		
		for (Row row : table)
			System.out.println(row);
	}
}

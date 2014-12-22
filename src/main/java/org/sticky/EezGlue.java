package org.sticky;

import static org.sticky.Glues.load;

import org.junit.Test;
import org.virtualrepository.csv.CsvAsset;
import org.virtualrepository.csv.CsvTable;
import org.virtualrepository.ows.Features;
import org.virtualrepository.ows.WfsFeatureType;
import org.virtualrepository.tabular.Row;
import org.virtualrepository.tabular.Table;

public class EezGlue {

	/**
	 * Requires investigation about Apache-Sis bug
	 * Switched off for now
	 * 
	 */
	@Deprecated
	@Test
	public void grabEez() {
		
		WfsFeatureType asset = new WfsFeatureType("marine-regions","MarineRegions:eez");
		
		asset.setService(Glues.vliz);
		
		//retrieve features
		Features features = Glues.repository.retrieve(asset, Features.class);
		
		//write features to gml
		Glues.storeAsGml("marine-regions.xml",features);		

	}
	
	@Test
	public void readEez(){
		
		CsvAsset asset = new CsvAsset("someid","marine-regions");
		
		asset.hasHeader(true);
		asset.setDelimiter(',');

		Table table = new CsvTable(asset, load("marine-regions.txt"));
		
		for (Row row : table)
			System.out.println(row);
		
	}
	
	
	@Deprecated
	@Test
	public void pushEez() {
	
		WfsFeatureType asset = new WfsFeatureType("marine-regions","marine-regions",Glues.grade);
		
		Glues.repository.publish(asset,Glues.load("marine-regions.xml"));

	}
	
}

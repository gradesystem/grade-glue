package org.sticky;

import java.io.InputStream;

import org.junit.Test;
import org.virtualrepository.ows.WfsFeatureType;

public class AdminUnitsGlue {

	@Test
	public void grabAdminUnits() {
		
		WfsFeatureType asset = new WfsFeatureType("fao-admin-units","GEONETWORK:g2008_1_12691");
		
		asset.setService(Glues.faodata);
		
		//retrieve features
		InputStream stream = Glues.repository.retrieve(asset, InputStream.class);
		
		//write to xml
		Glues.store("fao-admin-units.xml", stream);
	}
	
	@Test
	public void pushAdminUnits(){
		
		WfsFeatureType asset = new WfsFeatureType("fao-admin-units","fao-admin-units",Glues.grade);
		
		Glues.repository.publish(asset,Glues.load("fao-admin-units.xml"));
		
	}
	
}

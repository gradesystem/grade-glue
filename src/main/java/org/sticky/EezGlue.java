package org.sticky;

import java.io.InputStream;

import org.junit.Test;
import org.virtualrepository.ows.WfsFeatureType;

public class EezGlue {

	@Test
	public void grabMarineRegions() {
		
		WfsFeatureType asset = new WfsFeatureType("marine-regions","MarineRegions:eez");
		
		asset.setService(Glues.vliz);
		
		InputStream stream = Glues.repository.retrieve(asset, InputStream.class);
		
		Glues.store("marine-regions.xml",stream);
	}
	

	@Test
	public void pushMarineRegions() {
	
		
		WfsFeatureType asset = new WfsFeatureType("marine-regions","marine-regions",Glues.grade);
		
		Glues.repository.publish(asset,Glues.load("marine-regions.xml"));

	}
	
}

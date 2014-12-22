package org.sticky;

import java.io.InputStream;

import org.junit.Test;
import org.virtualrepository.ows.WfsFeatureType;

public class IntersectionGlue {

	@Test
	public void grabEezFsa() {
		
		WfsFeatureType asset = new WfsFeatureType("eez-fsa_intersection","GeoRelationship:FAO_AREAS_x_EEZ_HIGHSEAS");
		
		asset.setService(Glues.intersections);
		
		InputStream stream = Glues.repository.retrieve(asset, InputStream.class);
		
		Glues.store("intersections.xml",stream);
	}
	
	
	@Test
	public void pushEezFsa() {
	
		
		WfsFeatureType asset = new WfsFeatureType("eez-fsa_intersection","eez-fsa_intersection",Glues.grade);
		
		Glues.repository.publish(asset,Glues.load("intersections.xml"));

	}
	
	
}

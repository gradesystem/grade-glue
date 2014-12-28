package org.sticky;

import static org.sticky.Glues.*;

import java.io.InputStream;

import org.junit.Test;
import org.virtualrepository.ows.WfsFeatureType;

public class IntersectionGlue {

	@Test
	public void grabEezFsa() {
		
		WfsFeatureType asset = new WfsFeatureType("eez-fsa_intersection","GeoRelationship:FAO_AREAS_x_EEZ_HIGHSEAS");
		
		asset.setService(intersections);
		
		InputStream stream = repository.retrieve(asset, InputStream.class);
		
		store("intersections.xml",stream);
	}
	
	
	@Test
	public void pushEezFsa() {
	
		
		WfsFeatureType asset = new WfsFeatureType("eez-fsa_intersection","eez-fsa_intersection",grade);
		
		repository.publish(asset,load("intersections.xml"));

	}
	
	
}

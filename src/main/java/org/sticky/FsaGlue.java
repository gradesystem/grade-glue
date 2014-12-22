package org.sticky;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotoolkit.feature.FeatureTypeBuilder;
import org.geotoolkit.feature.simple.SimpleFeatureBuilder;
import org.geotoolkit.feature.simple.SimpleFeatureType;
import org.geotoolkit.feature.type.GeometryType;
import org.junit.Test;
import org.opengis.feature.AttributeType;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureType;
import org.opengis.feature.Property;
import org.opengis.feature.PropertyType;
import org.virtualrepository.ows.Features;
import org.virtualrepository.ows.WfsFeatureType;
import org.sticky.Glues;

public class FsaGlue {

	public static enum FsaLevel {
		
		MAJOR("MAJOR", "F_AREA"),
		SUBAREA("SUBAREA", "F_SUBAREA"),
		DIVISION("DIVISION", "F_DIVISION"),
		SUBDIVISION("SUBDIVISION", "F_SUBDIVIS"),
		SUBUNIT("SUBUNIT", "F_SUBUNIT");
		
		private final String value;
		private final String attribute;
		
		FsaLevel(String value, String attribute) {
			this.value=value;
			this.attribute=attribute;
		}
		
		public String value() {
			return value;
		}
		
		public String attribute(){
			return attribute;
		}
		
		public boolean before(FsaLevel fsa) {
			return this.ordinal()<fsa.ordinal();
		}
		
	}
	
	@Test
	public void grabFsaHierarchy() {
		
		WfsFeatureType asset = new WfsFeatureType("fifao-areas","fifao:FAO_AREAS");
		
		asset.setService(Glues.faoareas);
		
		//retrieve features
		Features features = Glues.repository.retrieve(asset, Features.class);
		
		//enrich features
		Features trgFeatures = buildFsaHierarchy(features);
		
		//write enriched features to GML (xml)
		Glues.storeAsGml("fao-areas.xml", trgFeatures);
	}

	@Test
	public void pushFsaHierarchy() {
	
		WfsFeatureType asset = new WfsFeatureType("fsa-hierarchy","fsa-hierarchy",Glues.grade);
		
		Glues.repository.publish(asset,Glues.load("fao-areas.xml"));

	}
	
	@SuppressWarnings("deprecation")
	public Features buildFsaHierarchy(Features features){
		
		//building enriched featureType
		FeatureType srcFeatureType = features.all().get(0).getType();
		final FeatureTypeBuilder ftb = new FeatureTypeBuilder();
		ftb.setName(srcFeatureType.getName().toString());

		
		for(PropertyType prop : srcFeatureType.getProperties(false)){
			if(prop instanceof GeometryType){
				ftb.add(prop.getName().toString(),
						((GeometryType) prop).getValueClass(),
						((GeometryType) prop).getCoordinateReferenceSystem());
				
			}else if(prop instanceof AttributeType){
				String name = prop.getName().toString();
				
				//reducing nb of properties for more transparency for grade
				if(name.equals("F_AREA")
				   || name.equals("F_SUBAREA")
				   || name.equals("F_DIVISION")
				   || name.equals("F_SUBDIVIS")
				   || name.equals("F_SUBUNIT")){
					name = name.replaceFirst("F_", "PARTOF_");
				}
				System.out.println(name);
				ftb.add(name,((AttributeType) prop).getValueClass());
			}
		}
		
		FeatureType trgFeatureType = ftb.buildSimpleFeatureType();
		
		//building new features
		List<Feature> trgFeatureList = new ArrayList<Feature>();	
		SimpleFeatureBuilder sfb = new SimpleFeatureBuilder((SimpleFeatureType) trgFeatureType);
		
		Iterator<Feature> it = features.all().iterator();
		while(it.hasNext()){
			
			String partOf = null;
			
			Feature f = it.next();
			String code = f.getPropertyValue("F_CODE").toString();
			
			//obtain parent code
			String level = f.getPropertyValue("F_LEVEL").toString();			
			
			FsaLevel fsaLevel = FsaLevel.valueOf(level);
			if(!fsaLevel.equals(FsaLevel.MAJOR)){
				FsaLevel parentFsaLevel = FsaLevel.values()[fsaLevel.ordinal()-1];
				partOf = f.getPropertyValue(parentFsaLevel.attribute()).toString();
				if(partOf.startsWith("_")){
					partOf = partOf.substring(1,partOf.length());
				}
			}
			
			//build new feature
			for(PropertyType prop : trgFeatureType.getProperties(false)){
				
				String propertyName = prop.getName().toString();
				if(propertyName.startsWith("PARTOF_")) propertyName = propertyName.replaceFirst("PARTOF_", "F_");
				Property property = f.getProperty(propertyName);
				
				String value = null;
				if(property != null && !propertyName.equals(fsaLevel.attribute())) {
					value = property.getValue().toString();
					if(value.startsWith("_")) value = value.substring(1,value.length());
				}
				
				sfb.add(value);
				
			}
			
			//add trgFeature
			trgFeatureList.add(sfb.buildFeature("fao-area-"+code));
		}
		
		Features trgFeatures = new Features(trgFeatureList);
		return(trgFeatures);
	}
	
}

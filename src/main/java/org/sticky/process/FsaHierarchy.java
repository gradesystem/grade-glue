package org.sticky.process;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotoolkit.feature.DefaultFeature;
import org.geotoolkit.feature.FeatureBuilder;
import org.geotoolkit.feature.FeatureTypeBuilder;
import org.geotoolkit.feature.simple.SimpleFeatureType;
import org.geotoolkit.feature.type.GeometryType;
import org.geotoolkit.feature.type.PropertyDescriptor;
import org.opengis.feature.AttributeType;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureType;
import org.opengis.feature.Property;
import org.opengis.feature.PropertyType;
import org.virtualrepository.ows.Features;

@SuppressWarnings("deprecation")
public class FsaHierarchy {

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
	
	
	public static Features buildFsaHierarchy(Features features){
		
		//building enriched featureType
		DefaultFeature feat = (DefaultFeature) features.all().get(0);
		org.geotoolkit.feature.type.FeatureType srcFeatureType = feat.getType();
		final FeatureTypeBuilder ftb = new FeatureTypeBuilder();
		ftb.setName(srcFeatureType.getName().toString());

		
		for(PropertyDescriptor prop : srcFeatureType.getDescriptors()){
			if(prop.getType() instanceof GeometryType){
				ftb.add(prop.getName().tip().toString(),
						((GeometryType) prop.getType()).getValueClass(),
						((GeometryType) prop.getType()).getCoordinateReferenceSystem());
				
			}else if(prop.getType() instanceof AttributeType){
				String name = prop.getName().tip().toString();
				
				//reducing nb of properties for more transparency for grade
				if(name.equals("F_AREA")
				   || name.equals("F_SUBAREA")
				   || name.equals("F_DIVISION")
				   || name.equals("F_SUBDIVIS")
				   || name.equals("F_SUBUNIT")){
					name = name.replaceFirst("F_", "PARTOF_");
				}
				
				if(!prop.getName().head().toString().contains("gml")){
					ftb.add(name,((AttributeType<?>) prop.getType()).getClass(),
							1, 1, true, null);
				}
			}
		}
		
		org.geotoolkit.feature.type.FeatureType trgFeatureType = ftb.buildSimpleFeatureType();
		
		//building new features
		List<Feature> trgFeatureList = new ArrayList<Feature>();	
		
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
			List<Object> values = new ArrayList<Object>();
			for(PropertyDescriptor prop : trgFeatureType.getDescriptors()){
				
				String propertyName = prop.getName().tip().toString();
				if(propertyName.startsWith("PARTOF_")) propertyName = propertyName.replaceFirst("PARTOF_", "F_");
				Property property = f.getProperty(propertyName);
				
				String value = null;
				if(property != null && !propertyName.equals(fsaLevel.attribute())) {
					value = property.getValue().toString();
					if(value.startsWith("_")) value = value.substring(1,value.length());
				}
				
				values.add(value);
				
			}
			
			//add trgFeature
			Feature trgFeature = FeatureBuilder.build((org.geotoolkit.feature.type.FeatureType) trgFeatureType, values, "fao-area-"+code);
			trgFeatureList.add(trgFeature);
			
		}
		
		Features trgFeatures = new Features(trgFeatureList);
		return(trgFeatures);
	}
	
}

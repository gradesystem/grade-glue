package org.sticky.process;

import static org.fao.fi.comet.mapping.dsl.MappingDSL.*;
import static org.fao.fi.comet.mapping.dsl.MappingDetailDSL.*;
import static org.fao.fi.comet.mapping.dsl.MappingElementDSL.*;
import static org.fao.fi.comet.mapping.model.utils.jaxb.JAXB2DOMUtils.*;
import static org.sticky.Common.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.fao.fi.comet.mapping.model.Mapping;
import org.fao.fi.comet.mapping.model.MappingData;
import org.fao.fi.comet.mapping.model.MappingElement;
import org.opengis.feature.Feature;
import org.sticky.jaxb.Code;
import org.sticky.jaxb.Country;
import org.sticky.jaxb.Eez;
import org.virtualrepository.RepositoryService;
import org.virtualrepository.ows.Features;
import org.virtualrepository.ows.WfsFeatureType;
import org.virtualrepository.tabular.Row;
import org.virtualrepository.tabular.Table;

/**
 * Glue to push EEZ data & related mappings to Grade
 * 
 * @author Emmanuel Blondel
 *
 */
public class Eezs {
	
	public static RepositoryService vliz = repository.services().lookup(new QName("vliz"));
	
	
	public static Features eezs() {
		
		WfsFeatureType asset = new WfsFeatureType("eez","MarineRegions:eez");
		asset.setService(vliz);
		
		return repository.retrieve(asset, Features.class);
	}

	/**
	* Get adhoc codelist (iso3code, name) built from the VLIZ EEZ layer
	* The iso3code is set as value, the name as key, as for some EEZs
	* the iso3code was used for several entries.
	*
	* This codelist is only an intermediary bridge to build the EEZs
	* mappings.
	*
	* @return the required codelist
	*/
	public static Map<String,List<String>> buildEmbeddedCodelist(Features features){
		Map<String,List<String>> codelist = new HashMap<String,List<String>>();
		
		Iterator<Feature> it = features.all().iterator();
		while (it.hasNext()) {
			Feature f = it.next();
			
			String eezIso = f.getPropertyValue("iso_3digit").toString();
			String eezName = f.getPropertyValue("country").toString();
			if (eezIso != null) {
				if (!codelist.keySet().contains(eezIso)) {
					List<String> names = new ArrayList<String>();
					names.add(eezName);
					codelist.put(eezIso, names);
				} else {
					List<String> names = codelist.get(eezIso);
					if (!names.contains(eezName)) {
						names.add(eezName);
						codelist.put(eezIso, names);
					}
				}
			}
		}
		
		return(codelist);
	}
	
	/**
	 * Builds the Sovereignty mapping: This mapping provides a 1-N relationship
	 * between a ISO3 code entity (representing a country) and one or more
	 * MarineRegions id (representing an EEZ - Exclusive Economic Zone), giving
	 * the sovereignty of country over EEZs. The mapping is entirely constructed
	 * from the VLIZ MarineRegions EEZ - MarBound database.
	 * 
	 * @param features
	 * @param codelist
	 * @return the comet mapping
	 */
	public static MappingData buildMappingSovereignty(Features features, Map<String,List<String>> codelist){

		//mapping description
		MappingData mappingData = new MappingData();
		mappingData.setDescription("This mapping provides a 1-N relationship between a ISO3 code entity "
								  +"(identifying a country) and one or more MarineRegions id (identifying "
								  +"an Exclusive Economic Zone - EEZ), giving the sovereignty of country "
								  +"over EEZs. The mapping is entirely constructed from the VLIZ Marine "
								  + "Regions MarBound database.");
		mappingData.setVersion("1.0");
		mappingData.setProducedOn(new Date());
		mappingData.setProducedBy("Emmanuel Blondel");	
		
		//iterate and build the mapping
		Iterator<String> it = codelist.keySet().iterator();
		while(it.hasNext()){
			
			//iso3code
			String srcCode = it.next();
			
			String message = null;
			if (srcCode.length() < 3) {
				message = srcCode
						+ " is not an ISO3 code."
						+ " Action required at source level";

			} else if (srcCode.length() > 3 || srcCode.contains("-")) {
				message = srcCode
						+ " is not an ISO3 code."
						+ " Composite EEZ detected. Action required at source level";
			}
			
			if(message == null){
			
				//features subset
				Predicate<Feature> codePredicate = p -> p
						.getPropertyValue("iso_3digit").toString().equals(srcCode);
				Predicate<Feature> namesPredicate = p -> codelist.get(srcCode)
						.contains(p.getPropertyValue("sovereign").toString());
				Predicate<Feature> fullPredicate = codePredicate.or(namesPredicate);
	
				List<Feature> filteredFeatures = features.all().stream()
						.filter(fullPredicate).collect(Collectors.toList());
				
				//sovereignty mapping
				Map<Integer,String> trgCodes = new HashMap<Integer,String>();
				if(filteredFeatures.size() > 0){
					Iterator<Feature> fit = filteredFeatures.iterator();
					while(fit.hasNext()){
						Feature f = fit.next();
						
						String sovName = f.getPropertyValue("sovereign").toString();	
						if(codelist.get(srcCode).contains(sovName)){
							String mrgid = f.getPropertyValue("mrgid").toString();
							Integer mrgidValue = Integer.valueOf(Double.valueOf(mrgid).intValue());
							if(!trgCodes.containsKey(mrgid)){
								String label = f.getPropertyValue("eez").toString();
								trgCodes.put(mrgidValue, label);
							}
						} 
					}
				}
				
				//comet mapping
				MappingElement source = wrap(asElement(Country.coding(new Code(srcCode, "iso3", null))));
				Mapping mappingEntry = map(source);
				
				Iterator<Integer> tit = trgCodes.keySet().iterator();
				while(tit.hasNext()){
					Integer trgCode = tit.next();
					String trgCodeLabel = trgCodes.get(trgCode);
					mappingEntry.to(target(wrap(asElement(Eez.coding(new Code(trgCode.toString(), "mrgid", trgCodeLabel))))));
				}

				mappingData.include(mappingEntry);
			}	
						
		}
		
		return(mappingData);

	}
	
	/**
	 * Builds the Exploitation rights mapping: This mapping provides a 1-N relationship
	 * between a ISO3 code entity (representing a flagstate) and one or more
	 * MarineRegions id (representing an EEZ - Exclusive Economic Zone), giving
	 * the concept of exploitation rights of a flagstate over EEZs. The mapping is constructed
	 * from the VLIZ MarineRegions EEZ - MarBound database and from the information if a country 
	 * is a flagstate
	 * 
	 * @param features
	 * @param codelist
	 * @return the comet mapping
	 */
	public static MappingData buildMappingExploitation(Features features,
												Map<String,List<String>> codelist,
												Table adminUnits){
		//mapping description
		MappingData mappingData = new MappingData();
		mappingData.setDescription("This mapping provides a 1-N relationship between a ISO3 code entity "
								  +"(identifying a flagstate) and one or more MarineRegions id (identifying "
								  +"an Exclusive Economic Zone - EEZ), giving the exploitation rights of flagstate "
								  +"over EEZs. is constructed from the VLIZ MarineRegions EEZ - MarBound database "
								  +"and from the information if a country is a flagstate");
		mappingData.setVersion("1.0");
		mappingData.setProducedOn(new Date());
		mappingData.setProducedBy("Emmanuel Blondel");
		
		//list of flagstates
		Map<String,Boolean> refadmin = new HashMap<String,Boolean>();
		Iterator<Row> fsit = adminUnits.iterator();
		while(fsit.hasNext()){
			Row row = fsit.next();
			String code = row.get("ISO3");
			Boolean isFlagstate = Boolean.valueOf(row.get("isFlagstate"));
			if(!refadmin.keySet().contains(code)){
				refadmin.put(code, isFlagstate);
			}
		}
		
		//iterate and build the mapping
		Iterator<String> it = codelist.keySet().iterator();
		while(it.hasNext()){
			
			//iso3code
			String srcCode = it.next();
			
			String message = null;
			if (srcCode.length() < 3) {
				message = srcCode
						+ " is not an ISO3 code."
						+ " Action required at source level";

			} else if (srcCode.length() > 3 || srcCode.contains("-")) {
				message = srcCode
						+ " is not an ISO3 code."
						+ " Composite EEZ detected. Action required at source level";
			}
			
			if(message == null){
				
				//isFlagstate?
				Boolean isFlagstate = refadmin.get(srcCode);
				if(isFlagstate == null) isFlagstate = false;
			
				//features subset
				Predicate<Feature> codePredicate = p -> p
						.getPropertyValue("iso_3digit").toString().equals(srcCode);
				Predicate<Feature> namesPredicate = p -> codelist.get(srcCode)
						.contains(p.getPropertyValue("sovereign").toString());
				Predicate<Feature> fullPredicate = codePredicate.or(namesPredicate);
	
				List<Feature> filteredFeatures = features.all().stream()
						.filter(fullPredicate).collect(Collectors.toList());
				
				//exploitation mapping
				Map<Integer,String> trgCodes = new HashMap<Integer,String>();
				if(filteredFeatures.size() > 0){
					Iterator<Feature> fit = filteredFeatures.iterator();
					while(fit.hasNext()){
						Feature f = fit.next();
						
						String eezCode = f.getPropertyValue("iso_3digit").toString();
						
						if(eezCode != null){
							if(eezCode.equals(srcCode)) {
								if(isFlagstate){
									String mrgid = f.getPropertyValue("mrgid").toString();
									Integer mrgidValue = Integer.valueOf(Double.valueOf(mrgid).intValue());
									if(!trgCodes.containsKey(mrgid)){
										String label = f.getPropertyValue("eez").toString();
										trgCodes.put(mrgidValue, label);
									}
								}

							} else {
								Boolean isEezFlagstate = refadmin.get(eezCode);
								if(isEezFlagstate == null) isEezFlagstate = false;
								if(!isEezFlagstate){
									String mrgid = f.getPropertyValue("mrgid").toString();
									Integer mrgidValue = Integer.valueOf(Double.valueOf(mrgid).intValue());
									if(!trgCodes.containsKey(mrgid)){
										String label = f.getPropertyValue("eez").toString();
										trgCodes.put(mrgidValue, label);
									}
								}
							}
							
						}
						
					}
				}
				
				//comet mapping
				MappingElement source = wrap(asElement(Country.coding(new Code(srcCode, "iso3", null))));
				Mapping mappingEntry = map(source);
				
				Iterator<Integer> tit = trgCodes.keySet().iterator();
				while(tit.hasNext()){
					Integer trgCode = tit.next();
					String trgCodeLabel = trgCodes.get(trgCode);
					mappingEntry.to(target(wrap(asElement(Eez.coding(new Code(trgCode.toString(), "mrgid", trgCodeLabel))))));
				}

				mappingData.include(mappingEntry);
			}	
						
		}
		
		return(mappingData);
	}
	
}

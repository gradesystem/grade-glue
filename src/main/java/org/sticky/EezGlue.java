package org.sticky;

import static org.fao.fi.comet.mapping.dsl.MappingDSL.map;
import static org.fao.fi.comet.mapping.dsl.MappingDetailDSL.target;
import static org.fao.fi.comet.mapping.dsl.MappingElementDSL.wrap;
import static org.fao.fi.comet.mapping.model.utils.jaxb.JAXB2DOMUtils.asElement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.fao.fi.comet.mapping.model.Mapping;
import org.fao.fi.comet.mapping.model.MappingData;
import org.fao.fi.comet.mapping.model.MappingElement;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.Feature;
import org.sticky.jaxb.Code;
import org.sticky.jaxb.Country;
import org.sticky.jaxb.Eez;
import org.virtualrepository.comet.CometAsset;
import org.virtualrepository.ows.Features;
import org.virtualrepository.ows.WfsFeatureType;
import org.virtualrepository.tabular.Table;

/**
 * Glue to push EEZ data & related mappings to Grade
 * 
 * @author Emmanuel Blondel
 *
 */
public class EezGlue {

	Features features;
	
	Map<String,List<String>> codelist;
	
	Table adminUnits;
	
	/**
	 * Retrieves and reads Eez
	 * 
	 */
	@Before
	public void before(){
		
		//read eez asset
		WfsFeatureType asset = new WfsFeatureType("eez","MarineRegions:eez");
		asset.setService(Glues.vliz);
		
		features = Glues.repository.retrieve(asset, Features.class);
		
		//prepare embedded Iso3 code list
		codelist = buildEmbeddedCodelist(features);
		
		//read admin units reference (with flagstate information)
		/*CsvAsset asset2 = new CsvAsset("someid","admin-units");
		asset2.hasHeader(true);
		asset2.setDelimiter(',');
		adminUnits = new CsvTable(asset2, load("admin-units.txt"));*/
		
	}
	
	
	/**
	 * Grabs the EEZ data
	 * 
	 */
	@Test
	public void grabEez() {

		Glues.storeAsGml("eez.xml",features);
		
	}

	/**
	 * Pushes the EEZ data
	 * 
	 */
	@Test
	public void pushEez() {
	
		WfsFeatureType asset = new WfsFeatureType("eez","eez",Glues.grade);
		
		Glues.repository.publish(asset,Glues.load("eez.xml"));

	}
	
	/**
	 * Grabs the Country-EEZ sovereignty mapping
	 * 
	 */
	@Test
	public void grabMappingSovereignty() {
		
		MappingData mapping = buildMappingSovereignty(features, codelist);
	
		Glues.storeMapping("eez-country-sovereignty.xml", mapping);
	}
	
	/**
	 * Pushes the Country-EEZ sovereignty mapping
	 * 
	 */
	@Test
	public void pushMappingSovereignty() {
	
		CometAsset asset = new CometAsset("eez-country-sovereignty",Glues.grade);
		
		Glues.repository.publish(asset,Glues.load("eez-country-sovereignty.xml"));

	}
	
	/**
	 * Grabs the Flagstate-EEZ exploitation rights mapping
	 * 
	 */
	@Test
	public void grabMappingExploitation() {
		
		MappingData mapping = buildMappingExploitation(features, codelist, adminUnits);
	
		Glues.storeMapping("eez-flagstate-exploitation.xml", mapping);
	}
	
	/**
	 * Pushes the Flagstate-EEZ exploitation rights mapping
	 * 
	 */
	@Test
	public void pushMappingExploitation() {
	
		CometAsset asset = new CometAsset("eez-flagstate-exploitation",Glues.grade);
		
		Glues.repository.publish(asset,Glues.load("eez-flagstate-exploitation.xml"));

	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	
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
	static Map<String,List<String>> buildEmbeddedCodelist(Features features){
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
	static MappingData buildMappingSovereignty(Features features, Map<String,List<String>> codelist){

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
	static MappingData buildMappingExploitation(Features features,
												Map<String,List<String>> codelist,
												Table adminUnits){
		//TODO business logic for exploitation rights
		return(null);
	}
	
}

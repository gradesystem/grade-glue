package org.sticky;

import static javax.ws.rs.client.ClientBuilder.*;
import static org.grade.client.upload.Grade.*;
import static org.sticky.Common.*;
import static org.sticky.Common.TestDeployment.*;
import static org.sticky.aux.AdminUnits.*;
import static org.sticky.aux.Eezs.*;
import static org.sticky.aux.FsaHierarchy.*;
import static org.sticky.aux.SpeciesDistribution.*;
import static org.sticky.aux.Worms.*;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.fao.fi.comet.mapping.model.MappingData;
import org.glassfish.jersey.filter.LoggingFilter;
import org.grade.client.upload.csv.Csv;
import org.junit.Test;
import org.virtualrepository.RepositoryService;
import org.virtualrepository.ows.Features;
import org.virtualrepository.ows.WfsFeatureType;
import org.virtualrepository.tabular.Table;

public class Glues {
	

	@Test
	public void grabRfb() {
		
		InputStream rfb = newClient()
							 .register(new LoggingFilter(Logger.getLogger(LoggingFilter.class.getName()),true))
							 .target(URI.create("http://www.fao.org/figis/moniker/figismapdata"))
							 .request().get(InputStream.class);
		
		store("rfb.xml",rfb);
		
		
	}
	
	@Test
	public void pushRfb() {
		
		drop(file("rfb.xml")).with(xml).in(ami).as("rfb");
		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public void grabAdminUnits() {

		Table countries = loadTable("gaul-codes.txt");
		Table flagstates = loadTable("flagstates.txt", csv().delimiter(';'));
		Table names = loadTable("gaul-names.txt",csv().delimiter('\t').encoding("UTF-16"));
		
		countries = enrichAdminUnitsTable(countries, flagstates);
		countries = buildAdminUnitsTable(countries, names);
		
		storeTable("admin-units.txt", countries, csv().encoding("UTF-16"));
	}
	
	@Test
	public void pushAdminUnits(){
		
		Csv csv = csv().delimiter(',').encoding("UTF-16");
		
		drop(file("admin-units.txt")).with(csv).in(ami).as("admin-units");
		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////	
	
	@Test
	public void grabEez() {

		Features eez = eezs();
		
		storeFeatures("eez.xml",eez);
		
	}
	
	@Test
	public void pushEez() {
	
		drop(file("eez.xml")).with(xml).in(ami).as("eez");
		
	}
	
	
	@Test
	public void grabMappingSovereignty() {
		
		Features eezs =  eezs(); 
		Map<String,List<String>> codelist = buildEmbeddedCodelist(eezs);
		MappingData mapping = buildMappingSovereignty(eezs, codelist);
	
		storeMapping("eez-country-sovereignty.xml", mapping);
	}

	@Test
	public void pushMappingSovereignty() {
	
		drop(file("eez-country-sovereignty.xml")).with(xml).in(ami).as("eez-country-sovereignty");
		
	}
	
	@Test
	public void grabMappingExploitation() {
		
		Table adminUnits = loadTable("admin-units.txt",csv().encoding("UTF-16"));
		Features eezs =  eezs(); 
		Map<String,List<String>> codelist = buildEmbeddedCodelist(eezs);
		
		MappingData mapping = buildMappingExploitation(eezs, codelist, adminUnits);
	
		storeMapping("eez-flagstate-exploitation.xml", mapping);
	}
	
	@Test
	public void pushMappingExploitation() {
	
		drop(file("eez-flagstate-exploitation.xml")).with(xml).in(ami).as("eez-flagstate-exploitation");
		

	}
	
	//////////////////////////////////////////////////////////////////////////////////////////	

	
	@Test
	public void grabFsaHierarchy() {
		
		RepositoryService faoareas = repository.services().lookup(new QName("fao-areas"));
		
		WfsFeatureType asset = new WfsFeatureType("fifao-areas","fifao:FAO_AREAS");
		
		asset.setService(faoareas);
		
		//retrieve features
		Features features = repository.retrieve(asset, Features.class);
		
		//enrich features
		Features trgFeatures = buildFsaHierarchy(features);
		
		//write enriched features to GML (xml)
		storeFeatures("fao-areas.xml", trgFeatures);
	}
	
	@Test
	public void pushFsaHierarchy() {
	
		drop(file("fao-areas.xml")).with(xml).in(ami).as("fsa-hierarchy");
		
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public void grabSpeciesDistributions(){
		
		String ep = "http://www.fao.org/figis/geoserver/species/ows";
		
		MappingData out = buildGeographicReferences(ep);
		
		storeMapping("species-distributions.xml", out);
		
	}
	
	
	@Test
	public void pushSpeciesDistributions(){
		
		drop(file("species-distributions.xml")).with(xml).in(ami).as("species-distributions");
		
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public void grabEezFsa() {
		
		RepositoryService intersections = repository.services().lookup(new QName("intersections"));
		
		WfsFeatureType asset = new WfsFeatureType("eez-fsa_intersection","GeoRelationship:FAO_AREAS_x_EEZ_HIGHSEAS");
		
		asset.setService(intersections);
		
		InputStream stream = repository.retrieve(asset, InputStream.class);
		
		store("intersections.xml",stream);
	}
	
	
	@Test
	public void pushEezFsa() {
	
		drop(file("intersections.xml")).with(xml).in(ami).as("eez-fsa_intersection");
		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public void grabWormsHierarchy() {
		
		Table worms = loadTable("worms-pisces.csv");
		MappingData wormsToAsfis = loadMapping("worms-to-asfis.xml");
		
		Table outHierarchy = buildWormsTaxonomicHierarchy(wormsToAsfis, worms);
		
		storeTable("worms-subset-hierarchy.txt", outHierarchy, csv().encoding("UTF-16").delimiter('\t'));
	}
	
	@Test
	public void pushWormsHierarchy(){
		
		Csv csv = csv().delimiter('\t').encoding("UTF-16");
		
		drop(file("worms-subset-hierarchy.txt")).with(csv).in(ami).as("worms-subset-hierarchy");
	
	}
	
	@Test
	public void grabWormsCodelist() {
		
		Table worms = loadTable("worms-pisces.csv");
		Table outHierarchy = loadTable("worms-subset-hierarchy.txt", 
							           csv().delimiter('\t').encoding("UTF-16"));
		
		Table outSubset = buildWormsSubset(outHierarchy, worms);
		
		storeTable("worms-subset-codelist.txt", outSubset, csv().encoding("UTF-16").delimiter('\t'));
	}
	
	@Test
	public void pushWormsCodelist(){	
		
		Csv csv = csv().delimiter('\t').encoding("UTF-16");
		
		drop(file("worms-subset-codelist.txt")).with(csv).in(ami).as("worms-subset-codelist");
	
		
	}
}

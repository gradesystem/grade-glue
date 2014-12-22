package org.sticky;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import javax.xml.namespace.QName;

import lombok.Cleanup;
import lombok.SneakyThrows;

import org.junit.Test;
import org.virtualrepository.RepositoryService;
import org.virtualrepository.VirtualRepository;
import org.virtualrepository.impl.Repository;
import org.virtualrepository.ows.WfsFeatureType;

public class Glues {
	
	static VirtualRepository repository = new Repository();
	static RepositoryService faoareas = repository.services().lookup(new QName("fao-areas"));
	static RepositoryService faodata = repository.services().lookup(new QName("data.fao.org"));
	static RepositoryService vliz = repository.services().lookup(new QName("vliz"));
	static RepositoryService intersections = repository.services().lookup(new QName("intersections"));
	static RepositoryService grade= repository.services().lookup(new QName("semantic-repository"));
	
	@Test
	public void smokeTest() {
		
		assertTrue(repository.services().size()>=2);
	}
	
	
	
	
	@Test
	public void grabMarineRegions() {
		
		WfsFeatureType asset = new WfsFeatureType("marine-regions","MarineRegions:eez");
		
		asset.setService(vliz);
		
		InputStream stream = repository.retrieve(asset, InputStream.class);
		
		store("marine-regions.xml",stream);
	}
	

	@Test
	public void pushMarineRegions() {
	
		
		WfsFeatureType asset = new WfsFeatureType("marine-regions","marine-regions",grade);
		
		repository.publish(asset,load("marine-regions.xml"));

	}
	
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
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	@SneakyThrows(Exception.class)
	static void store(String name, InputStream stream) {
		
		byte[] bytes = new byte[2048];
		
		File dest = new File("src/main/resources",name);
		
		@Cleanup FileOutputStream out = new FileOutputStream(dest);
	
		int read = 0;
		
		while ((read=stream.read(bytes))!=-1) 	out.write(bytes,0,read);
		
		out.flush();
			
	}
	
	@SneakyThrows(Exception.class)
	static InputStream load(String name) {
	
		return new FileInputStream(new File("src/main/resources",name));
				
	}
	
}

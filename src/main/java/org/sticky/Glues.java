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
	static RepositoryService vliz = repository.services().lookup(new QName("vliz"));
	
	@Test
	public void smokeTest() {
		
		assertTrue(repository.services().size()>=2);
	}
	
	@Test
	public void grabFaoAreas() {
		
		WfsFeatureType asset = new WfsFeatureType("fao-areas","area:FAO_AREAS");
		
		asset.setService(faoareas);
		
		InputStream stream = repository.retrieve(asset, InputStream.class);
		
		store("fao-areas.xml",stream);
	}
	
	@Test
	public void grabMarineRegions() {
		
		WfsFeatureType asset = new WfsFeatureType("marine-regions","MarineRegions:eez");
		
		asset.setService(vliz);
		
		InputStream stream = repository.retrieve(asset, InputStream.class);
		
		store("marine-regions.xml",stream);
	}
	

	
	@SneakyThrows(Exception.class)
	static void store(String name, InputStream stream) {
		
		byte[] bytes = new byte[2048];
		
		File dest = new File("src/main/resources",name);
		
		@Cleanup FileOutputStream out = new FileOutputStream(dest);
	
		while (stream.read(bytes)!=-1) 	out.write(bytes);
			
	}
	
	@SneakyThrows(Exception.class)
	static InputStream load(String name) {
	
		return new FileInputStream(new File("src/main/resources",name));
				
	}
	
}

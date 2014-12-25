package org.sticky;

import static javax.ws.rs.client.ClientBuilder.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import lombok.Cleanup;
import lombok.SneakyThrows;

import org.fao.fi.comet.mapping.model.MappingData;
import org.fao.fi.comet.mapping.model.utils.jaxb.JAXBDeSerializationUtils;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureStoreUtilities;
import org.geotoolkit.feature.xml.XmlFeatureWriter;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureWriter;
import org.glassfish.jersey.filter.LoggingFilter;
import org.junit.Test;
import org.opengis.feature.Feature;
import org.virtualrepository.RepositoryService;
import org.virtualrepository.VirtualRepository;
import org.virtualrepository.impl.Repository;
import org.virtualrepository.ows.Features;
import org.virtualrepository.ows.WfsFeatureType;

public class Glues {
	
	static VirtualRepository repository = new Repository();
	static RepositoryService faoareas = repository.services().lookup(new QName("fao-areas"));
	static RepositoryService vliz = repository.services().lookup(new QName("vliz"));
	static RepositoryService intersections = repository.services().lookup(new QName("intersections"));
	static RepositoryService grade= repository.services().lookup(new QName("semantic-repository"));
	
	@Test
	public void smokeTest() {
		
		assertTrue(repository.services().size()>=2);
	}
	
	
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
		
		WfsFeatureType asset = new WfsFeatureType("rfb","rfb",grade);
		
		repository.publish(asset,load("rfb.xml"));
		
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
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	@SneakyThrows(Exception.class)
	static void store(String name, Features features){
		
		@Cleanup OutputStream out = new FileOutputStream(new File("src/main/resources", name));
		XmlFeatureWriter featureWriter = new JAXPStreamFeatureWriter();
		
		final Collection<org.geotoolkit.feature.Feature> geotkFeatures = new ArrayList<org.geotoolkit.feature.Feature>();
		for(Feature f : features.all()){
			geotkFeatures.add((org.geotoolkit.feature.Feature) f);
		}
		final FeatureCollection<org.geotoolkit.feature.Feature> fc = FeatureStoreUtilities
				.collection((org.geotoolkit.feature.type.FeatureType) features.all().get(0).getType(), geotkFeatures);
		
		featureWriter.write(fc, out);
	}
	
	@SneakyThrows(Exception.class)
	static void store(String name, MappingData mapping){
		
		String xml = JAXBDeSerializationUtils.toXML(mapping);
		
		File dest = new File("src/main/resources",name);
		
		@Cleanup FileOutputStream out = new FileOutputStream(dest);
		
		out.write(xml.getBytes());
		out.flush();
		
	}
	
	@SneakyThrows(Exception.class)
	static InputStream load(String name) {
	
		return new FileInputStream(new File("src/main/resources",name));
				
	}
	
}

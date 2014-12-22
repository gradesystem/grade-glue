package org.sticky;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;

import lombok.Cleanup;
import lombok.SneakyThrows;

import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureStoreUtilities;
import org.geotoolkit.feature.xml.XmlFeatureWriter;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureWriter;
import org.junit.Test;
import org.opengis.feature.Feature;
import org.virtualrepository.RepositoryService;
import org.virtualrepository.VirtualRepository;
import org.virtualrepository.impl.Repository;
import org.virtualrepository.ows.Features;

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
	static void storeAsGml(String name, Features features){
		
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
	static InputStream load(String name) {
	
		return new FileInputStream(new File("src/main/resources",name));
				
	}
	
}

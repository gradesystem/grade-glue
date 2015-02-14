package org.sticky;

import static java.nio.file.Files.*;
import static java.nio.file.Paths.*;
import static java.nio.file.StandardCopyOption.*;
import static org.fao.fi.comet.mapping.model.utils.jaxb.JAXBDeSerializationUtils.*;
import static org.grade.client.upload.Grade.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import lombok.Cleanup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.fao.fi.comet.mapping.model.MappingData;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureStoreUtilities;
import org.geotoolkit.feature.xml.XmlFeatureWriter;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureWriter;
import org.grade.client.upload.Deployment;
import org.grade.client.upload.csv.Csv;
import org.opengis.feature.Feature;
import org.virtualrepository.VirtualRepository;
import org.virtualrepository.csv.CsvAsset;
import org.virtualrepository.csv.CsvTable;
import org.virtualrepository.impl.Repository;
import org.virtualrepository.ows.Features;
import org.virtualrepository.tabular.Column;
import org.virtualrepository.tabular.Row;
import org.virtualrepository.tabular.Table;

public class Common {

	public static VirtualRepository repository = new Repository();
	
	
	@RequiredArgsConstructor
	@Getter
	public static enum TestDeployment implements Deployment {
		
		ami("http://grade.ddns.net:8080","upload"),
		preproduction("http://figisapps.fao.org/grade","upload");
		
		final String uri; 
		final String endpoint;
		
	}
	
	
	/////////////////////////////////////////////////
	
	public static String file(String name) {
		return "src/main/resources/"+name;
	}
	
	@SneakyThrows
	public static void store(String name, InputStream stream) {
		
		copy(stream, get(file(name)), REPLACE_EXISTING);
			
	}
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	@SneakyThrows
	public static void storeFeatures(String name, Features features){
		
		@Cleanup OutputStream out = new FileOutputStream(new File(file(name)));
		XmlFeatureWriter featureWriter = new JAXPStreamFeatureWriter();
		
		final Collection<org.geotoolkit.feature.Feature> geotkFeatures = new ArrayList<org.geotoolkit.feature.Feature>();
		for(Feature f : features.all()){
			geotkFeatures.add((org.geotoolkit.feature.Feature) f);
		}
		final FeatureCollection<org.geotoolkit.feature.Feature> fc = FeatureStoreUtilities
				.collection((org.geotoolkit.feature.type.FeatureType) features.all().get(0).getType(), geotkFeatures);
		
		featureWriter.write(fc, out);
	}
	
	
	public static void storeTable(String name, Table table){
		storeTable(name,table,csv());
	}
	
	@SneakyThrows
	public static void storeTable(String name, Table table, Csv info){
		
		File file = new File(file(name));
		OutputStream os = new FileOutputStream(file);
		
		OutputStreamWriter osw = new OutputStreamWriter(os, info.encoding()); 
		
		@Cleanup BufferedWriter bufferedWriter = new BufferedWriter(osw);
		
		//writing header
		List<Column> columns = table.columns();
		for(int i=0;i<columns.size();i++){
			bufferedWriter.append(columns.get(i).name().toString());
			if(i < columns.size()-1) bufferedWriter.append(info.delimiter());
		}
		bufferedWriter.newLine();
		
		//writing content
		Iterator<Row> it = table.iterator();
		while(it.hasNext()){
			Row row = it.next();
			for(int i=0;i<columns.size();i++){
				bufferedWriter.append(row.get(columns.get(i)));
				if(i < columns.size()-1) bufferedWriter.append(info.delimiter());
			}
			
			if(it.hasNext()) bufferedWriter.newLine();
		}
		
		bufferedWriter.flush();
		
	}
	
	@SneakyThrows
	public static void storeMapping(String name, MappingData mapping){
		
		write(get(file(name)), toXML(mapping).getBytes());
		
	}
	
	//////////////////////////////////////////////
	
	
	@SneakyThrows
	public static InputStream load(String name) {
	
		return new FileInputStream(file(name));
				
	}
	
	public static Table loadTable(String file){
		
		return loadTable(file,csv());
	}
	
	@SneakyThrows
	public static Table loadTable(String file, Csv info){
	
		CsvAsset a = new CsvAsset("someid","somename");
		a.hasHeader(true);
		a.setDelimiter(info.delimiter());
		a.setEncoding(Charset.forName(info.encoding()));
		a.setQuote(info.quote());
		return new CsvTable(a, load(file));
	}
	
	@SneakyThrows(Exception.class)
	public static MappingData loadMapping(String name){
		
		@Cleanup
		InputStream is = load(name);
		
		return fromSource(new StreamSource(is));
	}
}

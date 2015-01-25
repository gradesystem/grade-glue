package org.sticky;

import static org.sticky.Glues.grade;
import static org.sticky.Glues.load;
import static org.sticky.Glues.loadMapping;
import static org.sticky.Glues.repository;
import static org.sticky.Glues.store;
import static java.util.stream.Collectors.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.fao.fi.comet.mapping.model.Mapping;
import org.fao.fi.comet.mapping.model.MappingData;
import org.fao.fi.comet.mapping.model.MappingDetail;
import org.junit.Before;
import org.junit.Test;
import org.virtualrepository.csv.CsvAsset;
import org.virtualrepository.csv.CsvCodelist;
import org.virtualrepository.csv.CsvTable;
import org.virtualrepository.tabular.Column;
import org.virtualrepository.tabular.DefaultTable;
import org.virtualrepository.tabular.Row;
import org.virtualrepository.tabular.Table;

/**
 * Worms Glue
 * 
 * At now, this glue allows to push a worms-asfis mapping enriched with Worms
 * detailed information, by inputing a pure asfis-worms code mapping (in comet
 * format), and a reference worms dataset.
 * 
 * @author eblondel
 *
 */
public class WormsGlue {

	Table wormsDataset;
	
	MappingData wormsToAsfis;
	
	@Before
	public void before(){
		
		//read worms asset
		CsvAsset wormsAsset = new CsvAsset("someid","worms-pisces");
		wormsAsset.hasHeader(true);
		wormsAsset.setDelimiter(',');
		wormsDataset = new CsvTable(wormsAsset, load("worms-pisces.csv"));
		
		//read worms-to-asfis mapping asset
		wormsToAsfis = loadMapping("worms-to-asfis.xml");
	}
	
	@Test
	public void grabWormsSubset() {
		
		Table out = enrichMappingWithWormsDetails(wormsToAsfis, wormsDataset);
		
		store("worms-to-asfis-enriched.txt", out, "UTF16", '\t');
		
	}
	
	@Test
	public void pushWormsSubset(){
		
		CsvCodelist asset = new CsvCodelist("worms-to-asfis-enriched",0, grade);
		
		asset.hasHeader(true);
		asset.setDelimiter('\t');
		asset.setEncoding(Charset.forName("UTF-16"));
		
		Table table = new CsvTable(asset,load("worms-to-asfis-enriched.txt"));
		
		repository.publish(asset, table);
		
	}
	
	/**
	 * Some facility to filter the WoRMS dataset based on the worms-to-asfis comet mapping
	 * The source code (alphacode) is also added, hence the result provides an enriched
	 * asfis - worms.
	 * 
	 * @param mappingData
	 * @param datasetToFilter
	 * @return 
	 */
	static Table enrichMappingWithWormsDetails(MappingData mappingData, Table datasetToFilter){
		
		List<Row> trgRows = new ArrayList<Row>();
		
		//new columns
		List<Column> columns = new ArrayList<Column>();
		columns.add(new Column("asfisId"));
		columns.add(new Column("wormsId"));
		for(Column col : datasetToFilter.columns()){
			if(!col.name().getLocalPart().equals("id")) columns.add(col);
		}
		
		//name row list
		List<Row> nameRows = new ArrayList<Row>();
		Iterator<Row> rit = datasetToFilter.iterator();
		while(rit.hasNext()){
			Row nameRow = rit.next();
			
			Map<QName,String> data = new HashMap<QName,String>();
			for(Column col : datasetToFilter.columns()){
				data.put(col.name(), nameRow.get(col));
			}
			
			Row newRow = new Row(data);
			nameRows.add(newRow);
		}
		
		//iterating on the worms-to-asfis pure code mapping
		Collection<Mapping> mappings = mappingData.getMappings();
		Iterator<Mapping> it = mappings.iterator();
		while(it.hasNext()){
			
			Mapping mapping = it.next();
			
			//asfis identifier (alphacode)
			String alphacode = mapping.getSource().getId().getElementId().toString().split("urn:")[1];
			
			//worms identifier (aphiaId)
			List<MappingDetail> details = (List<MappingDetail>) mapping.getTargets();
			MappingDetail target = details.get(0);
			String aphiaId = target.getTargetElement().getId().getElementId().toString().split("urn:")[1];
			
			//filter worms data
			Row srcRow = nameRows.stream().filter(p -> p.get("id").equals(aphiaId)).collect(toList()).get(0);
			
			//building new row
			Map<QName,String> data = new HashMap<QName,String>();
			data.put(columns.get(0).name(), alphacode);
			data.put(columns.get(1).name(), aphiaId);
			
			for(Column col : datasetToFilter.columns()){
				if(!col.name().getLocalPart().equals("id")){
					data.put(col.name(), srcRow.get(col));
				}
			}
			
			Row trgRow = new Row(data);
			trgRows.add(trgRow);			
			
		}
		
		Table output = new DefaultTable(columns, trgRows.iterator());
		return output;
	}
	
}

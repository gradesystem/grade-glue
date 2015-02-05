package org.sticky.aux;

import static java.util.stream.Collectors.*;

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
import org.virtualrepository.tabular.Column;
import org.virtualrepository.tabular.DefaultTable;
import org.virtualrepository.tabular.Row;
import org.virtualrepository.tabular.Table;

/**
 * Worms Glue
 * 
 * At now, this glue allows to push (1) a pure 'AphiaId' taxonomic hierarchy and
 * (2) a subset of Worms giving the 'AphiaId' and textual information for all
 * taxonomic levels, covering the ASFIS list of species.
 * 
 * The glue requires a pure asfis-worms code mapping (in comet format) for
 * filtering purpose based on ASFIS, and a reference worms dataset.
 * 
 * @author eblondel
 *
 */
public class Worms {

	
	/**
	 * Some facility to build a pure 'AphiaId' taxonomic hierarchy. The business logic handles
	 * a filter based on the ASFIS-WORMS pure code MappingData.
	 * 
	 * @param mappingData
	 * @param datasetToFilter
	 * @return a virtual-repository Table
	 */
	public static Table buildWormsTaxonomicHierarchy(MappingData mappingData, Table datasetToFilter){
		
		List<Row> trgRows = new ArrayList<Row>();
		
		//new columns
		List<Column> columns = new ArrayList<Column>();
		columns.add(new Column("species"));
		columns.add(new Column("genus"));
		columns.add(new Column("family"));
		columns.add(new Column("order"));
		columns.add(new Column("class"));
		columns.add(new Column("phylum"));
		columns.add(new Column("kingdom"));
		
		//name row list
		List<Row> nameRows = toRowsList(datasetToFilter);
		
		//iterating on the worms-to-asfis pure code mapping
		Collection<Mapping> mappings = mappingData.getMappings();
		int total = mappings.size();
		int count = 1;
		Iterator<Mapping> it = mappings.iterator();
		while(it.hasNext()){
			
			Mapping mapping = it.next();
			
			//worms identifier (aphiaId)
			List<MappingDetail> details = (List<MappingDetail>) mapping.getTargets();
			MappingDetail target = details.get(0);
			String aphiaId = target.getTargetElement().getId().getElementId().toString().split("urn:")[1];
			
			//filter worms data
			Row srcRow = nameRows.stream().filter(p -> p.get("id").equals(aphiaId)).collect(toList()).get(0);
			
			//building new row
			Map<QName,String> data = new HashMap<QName,String>();
			
			for(Column col : columns){
				
				if(col.name().getLocalPart().equals("species")){
					data.put(col.name(), aphiaId);
					
				}else{
					String content = srcRow.get(col.name().getLocalPart());
					if(!content.equals("")){
						Row refRow = nameRows.stream().filter(p -> p.get("scientific_name").equals(content)).collect(toList()).get(0);
						String otherAphiaId = refRow.get("id");
						data.put(col.name(), otherAphiaId);
					}
				}

			}
			
			Row trgRow = new Row(data);
			trgRows.add(trgRow);
		
			System.out.println(count+" / "+total);
			count++;
			
		}
		
		Table output = new DefaultTable(columns, trgRows.iterator());
		return output;
	}
	
	/**
	 * Some facility to filter out the WoRMS dataset for all
	 * taxonomic levels covered in the WorMs hierarchical dataset
	 * produced (filtered on ASFIS species list)
	 * 
	 * @param hierarchicalDataset
	 * @return a virtual-repository Table
	 */
	public static Table buildWormsSubset(Table hierarchicalDataset, Table nameDataset) {
		
		List<Row> trgRows = new ArrayList<Row>();
		
		List<Column> columns = new ArrayList<Column>();
		columns.add(new Column("aphiaId"));
		columns.add(new Column("name"));
		columns.add(new Column("rank"));
		columns.add(new Column("author"));
		
		List<Column> srcColumns = hierarchicalDataset.columns();
		
		List<Row> nameRows = toRowsList(nameDataset);
		Iterator<Row> it = hierarchicalDataset.iterator();
		while(it.hasNext()){
			Row codeRow = it.next();
			
			for(Column col : srcColumns){
				
				String aphiaId = codeRow.get(col);
				
				if(!aphiaId.equals("") && !aphiaId.equals("null")){
					boolean yetRetrieved = trgRows.stream().filter(p -> p.get("aphiaId").equals(aphiaId)).collect(toList()).size() > 0;
					if(!yetRetrieved){

						Row nameRow = nameRows.stream().filter(p -> p.get("id").equals(aphiaId)).collect(toList()).get(0);
						
						Map<QName,String> data = new HashMap<QName,String>();
						data.put(columns.get(0).name(), aphiaId);
						data.put(columns.get(1).name(), nameRow.get("scientific_name"));
						data.put(columns.get(2).name(), col.name().getLocalPart());
						data.put(columns.get(3).name(), nameRow.get("author"));
						
						Row trgRow = new Row(data);
						trgRows.add(trgRow);		
					}
				}
			}
		}
		
		Table output = new DefaultTable(columns, trgRows.iterator());
		return output;
	}
	
	/**
	 * Some missing VR util?
	 * 
	 * @param table
	 * @return
	 */
	public static List<Row> toRowsList(Table table){
		
		List<Row> nameRows = new ArrayList<Row>();
		Iterator<Row> rit = table.iterator();
		while(rit.hasNext()){
			Row nameRow = rit.next();
			
			Map<QName,String> data = new HashMap<QName,String>();
			for(Column col : table.columns()){
				data.put(col.name(), nameRow.get(col));
			}
			
			Row newRow = new Row(data);
			nameRows.add(newRow);
		}
		return(nameRows);
	}
	
}

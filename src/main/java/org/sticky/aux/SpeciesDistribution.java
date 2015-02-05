package org.sticky.aux;

import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;
import static javax.ws.rs.client.ClientBuilder.*;
import static javax.ws.rs.core.HttpHeaders.*;
import static org.fao.fi.comet.mapping.dsl.MappingDSL.*;
import static org.fao.fi.comet.mapping.dsl.MappingDetailDSL.*;
import static org.fao.fi.comet.mapping.dsl.MappingElementDSL.*;
import static org.fao.fi.comet.mapping.model.utils.jaxb.JAXB2DOMUtils.*;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.WebTarget;

import org.fao.fi.comet.mapping.model.Mapping;
import org.fao.fi.comet.mapping.model.MappingData;
import org.fao.fi.comet.mapping.model.MappingElement;
import org.geotoolkit.wms.xml.AbstractLayer;
import org.geotoolkit.wms.xml.v130.Identifier;
import org.geotoolkit.wms.xml.v130.Layer;
import org.geotoolkit.wms.xml.v130.MetadataURL;
import org.geotoolkit.wms.xml.v130.WMSCapabilities;
import org.glassfish.jersey.client.filter.EncodingFilter;
import org.glassfish.jersey.message.GZipEncoder;
import org.sticky.jaxb.Code;
import org.sticky.jaxb.Entity;
import org.sticky.jaxb.GeographicReference;

/**
 * Glue to push Gis mappings to Grade. By Gis mapping, we mean the mapping
 * between LOD domain object URIs (that have been used by GEMS to annotate OGC
 * services, data & metadata assets), and GIS product URIs (represented by the
 * xml OGC metadata published in Geonetwork)
 * 
 * 
 * @author Emmanuel Blondel
 *
 */
public class SpeciesDistribution {
	
	/**
	 * Builds the mapping between a FLOD domain object uri and a Geonetwork
	 * GIS metadata uri
	 * 
	 * @return the mapping in comet format
	 */
	public static MappingData buildGeographicReferences(String endpoint){
		
		//read WMS GetCapabilities document
		Client client = newClient()
				.register(ContentTypeFixer.instance)
				.register(GZipEncoder.class)
				.register(EncodingFilter.class);
		
		WebTarget target = client.target(endpoint)
								.queryParam("service", "wms")
								.queryParam("version", "1.3.0")
								.queryParam("request", "GetCapabilities");
		
		WMSCapabilities capabilities = target.request().get(WMSCapabilities.class);
		
		//prepare mapping data
		MappingData mappingData = new MappingData();
		mappingData.setDescription("Mapping between LOD entities and geographic references from "
								  +"the following GIS collection: "+capabilities.getService().getTitle());
		mappingData.setVersion("1.0");
		mappingData.setProducedOn(new Date());
		mappingData.setProducedBy("Emmanuel Blondel");
		
		//extract relevant piece of information and build the comet mapping on-the-fly
		Iterator<AbstractLayer> it = capabilities.getLayers().iterator();
		while(it.hasNext()){

			Layer layer = (Layer) it.next();
			
			//LOD reference
			Identifier id = null;
			if(layer.getIdentifier().size() > 0){
				
				List<Identifier> ids = layer.getIdentifier().stream()
										  .filter(p -> p.getAuthority().equals("FLOD"))
										  .collect(toList());
				if(ids.size() > 0)
					id = ids.get(0);
			}
			
			//GIS reference
			MetadataURL md = null;
			if(layer.getMetadataURL().size() > 0){
				
				Predicate<MetadataURL> formatPred = p -> p.getFormat().equals("text/xml");
				Predicate<MetadataURL> typePred = p -> p.getType().equals("ISO19115:2003");
				List<MetadataURL> mds = layer.getMetadataURL().stream()
										   .filter(formatPred.and(typePred))
										   .collect(toList());
				if(mds.size() > 0)
					md = mds.get(0);
			}
			
			// comet mapping
			if (id != null && md != null) {
				MappingElement source = wrap(asElement(Entity.coding(
						new Code(id.getValue(), "uri", null))));
				Mapping mappingEntry = map(source);

				mappingEntry.to(target(wrap(asElement(GeographicReference.coding(
						new Code(md.getOnlineResource().getHref(),"uri", layer.getTitle()))))));
				mappingData.include(mappingEntry);
			}

		}	
		
		return mappingData;
	}
	

	private static class ContentTypeFixer implements ClientResponseFilter  {
		
		static final ContentTypeFixer instance = new ContentTypeFixer();
		
		@Override
		public void filter(ClientRequestContext requestContext,
				ClientResponseContext responseContext) throws IOException {
			
			if (responseContext.getHeaderString(CONTENT_TYPE).contains("subtype=gml"))
				responseContext.getHeaders().put(CONTENT_TYPE, asList("text/xml"));
			
		}
	}
	

}

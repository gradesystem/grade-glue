package org.sticky;

import static au.com.bytecode.opencsv.CSVReader.*;
import static java.lang.String.*;
import static java.util.logging.Logger.*;
import static javax.ws.rs.client.ClientBuilder.*;
import static javax.ws.rs.client.Entity.*;
import static javax.ws.rs.core.MediaType.*;
import static lombok.AccessLevel.*;
import static org.glassfish.jersey.media.multipart.ContentDisposition.*;
import static org.sticky.Glues.*;

import java.io.InputStream;
import java.nio.charset.Charset;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.Status.Family;

import lombok.Cleanup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.GZipEncoder;

import com.fasterxml.jackson.annotation.JsonProperty;

@Setter(PRIVATE)
public class GradePublisher {
	
	//param vocabs
	@RequiredArgsConstructor
	public static enum Deployment {
		
		ami("http://grade.ddns.net:8080","staging"),
		preproduction("http://figisapps/grade","staging");
		
		final String uri; 
		final String ep;
		
	}
	
	
	@Data
	@EqualsAndHashCode(callSuper=false)
	public static class Csv extends UploadInfo {

		static final String cpart_name = "content";
		static final String ipart_name = "info";
		
		@JsonProperty 
		private char delimiter = DEFAULT_SEPARATOR;
		
		@NonNull @JsonProperty 
		private String encoding = Charset.defaultCharset().name();
		
		@JsonProperty 
		private char quote = DEFAULT_QUOTE_CHARACTER;
		
		
		Csv() {
			super(new MediaType("csv","text"),"csv");
		}
		
		@SneakyThrows
		Entity<?> bodyWith(InputStream content) {
			
			ContentDisposition content_part = type("form-data;name=\""+cpart_name+"\"").build();
			ContentDisposition info_part = type("form-data;name=\""+ipart_name+"\"").build();
			
			BodyPart cpart = new BodyPart(content,new MediaType("text","csv")).contentDisposition(content_part);
			BodyPart ipart = (new BodyPart(this, APPLICATION_JSON_TYPE)).contentDisposition(info_part);
			
			@Cleanup MultiPart multipart = new MultiPart().bodyPart(cpart).bodyPart(ipart);
			
			return entity(multipart,MULTIPART_FORM_DATA_TYPE);
		}
	}
	
	public static UploadInfo xml = new UploadInfo(APPLICATION_XML_TYPE,"xml");
	public static UploadInfo json = new UploadInfo(APPLICATION_JSON_TYPE,"json");
	public static Csv csv() { return new Csv();}
	
	//dsl
	@FunctionalInterface
	public interface TypeClause { TargetClause with(UploadInfo type);}
	
	@FunctionalInterface
	public interface InfoClause { TargetClause with(UploadInfo type);}
	@FunctionalInterface
	public interface TargetClause { NameClause in(Deployment target); }
	@FunctionalInterface
	public interface NameClause { void as(String name) throws WebApplicationException; }
	
	
	@NonNull
	private Deployment deployment;
	
	@NonNull
	private UploadInfo info; 
	
	@NonNull
	InputStream content;
	
	
	static TypeClause drop(@NonNull String file) {
	
		return drop(load(file));
		
	}
	
	static TypeClause drop(InputStream c) {
		
		GradePublisher $ = new GradePublisher();
		
		$.content(c);
		
		return (type) -> {
			
			$.info(type);
			
			return (dpl)-> {
				
				$.deployment(dpl);

				return $::in; 
			};
		};
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	@RequiredArgsConstructor
	private static class UploadInfo{

		@NonNull
		private final MediaType media; 
		
		@NonNull
		private final String path; 
		
		Entity<?> bodyWith(InputStream content) {
			return entity(content,new Variant(media,(String)null,"gzip"));	
		}
	
	};


	
	private static ClientBuilder maker = newBuilder()
											.register(MultiPartFeature.class)
											.register(GZipEncoder.class)
											.register(new LoggingFilter(getLogger(LoggingFilter.class.getName()),true));
	
	private static String api_template = "service/stage/endpoint/%s/dropin/%s/%s";
	
	
	private void in(@NonNull String label) {
		
		String path = format(api_template,deployment.ep,info.path,label);
		
		Response response = maker.build().target(deployment.uri).path(path).request().post(info.bodyWith(content));
		
		if (!response.getStatusInfo().getFamily().equals(Family.SUCCESSFUL))
			throw new WebApplicationException(response);
	}
}

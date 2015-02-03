package org.sticky;

import static javax.ws.rs.client.ClientBuilder.*;
import static javax.ws.rs.client.Entity.*;
import static javax.ws.rs.core.MediaType.*;
import static lombok.AccessLevel.*;
import static org.sticky.Glues.*;

import java.io.InputStream;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.glassfish.jersey.filter.LoggingFilter;

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
	
	@RequiredArgsConstructor
	public static enum UploadType { 
		
		xml (APPLICATION_XML), 
		json (APPLICATION_JSON), 
		csv ("text/csv"); 
		
		final String media; 
	}

	//dsl
	public interface TypeClause { TargetClause with(UploadType type);}
	
	public interface TargetClause { NameClause in(Deployment target); }
	
	public interface NameClause { void as(String name) throws WebApplicationException; }
	
	
	@NonNull
	private Deployment deployment;
	
	@NonNull
	private UploadType type; 
	
	@NonNull
	InputStream content;
	
	
	static TypeClause drop(@NonNull String file) {
	
		return drop(load(file));
		
	}
	
	static TypeClause drop(@NonNull InputStream c) {
		
		final GradePublisher $ = new GradePublisher();
		
		$.content(c);
		
		return (type) -> {
			
			$.type(type);
			
			return (dpl)-> {
				
				$.deployment(dpl);

				return $::in; 
			};
		};
	}
	
	private static ClientBuilder maker = newBuilder()
											.register(new LoggingFilter(Logger.getLogger(LoggingFilter.class.getName()),true));
	
	
	private void in(@NonNull String label) {
		
		String path = "service/stage/endpoint/"+ deployment.ep + "/dropin/" + type.name() +"/"+label;
		
		Response response = maker.build().target(deployment.uri).path(path).request().post(entity(content,type.media));
		
		if (!response.getStatusInfo().getFamily().equals(Family.SUCCESSFUL))
			throw new WebApplicationException(response);
	}
}

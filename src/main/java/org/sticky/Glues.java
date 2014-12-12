package org.sticky;

import static org.junit.Assert.*;

import org.junit.Test;
import org.virtualrepository.VirtualRepository;
import org.virtualrepository.impl.Repository;

public class Glues {


	static VirtualRepository repository = new Repository();
	
	@Test
	public void smokeTest() {
		
		assertTrue(repository.services().size()>=2);
	}
}

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;

public class IntermediateTasksTest {

	@Test
	public void calculateAverageTestEmptyList() {
		ArrayList<Long> times = new ArrayList<>();
		assertTrue(IntermediateTasks.calculateAverage(times) - 0 < 0.01);
	}
	
	@Test
	public void calculateAverageTestOneItem() {
		ArrayList<Long> times = new ArrayList<>();
		times.add((long)1);
		assertTrue(IntermediateTasks.calculateAverage(times) - 1 < 0.01);
	}
	
	@Test
	public void calculateAverageTestMoreThanOne() {
		ArrayList<Long> times = new ArrayList<>();
		times.add((long) 1);
		times.add((long) 2);
		times.add((long) 3);
		assertTrue(IntermediateTasks.calculateAverage(times) - 2 < 0.01);
	}
	
	@Test
	public void makeRequestTest() {
		FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));
        CacheControlDirective cache = new CacheControlDirective();
        
        Bundle responseTest = IntermediateTasks.makeRequest(client, "SMITH", cache);
        
        Bundle responseActual = client
	        				.search()
					        .forResource("Patient")
					        .where(Patient.FAMILY.matches().value("SMITH"))
					        .returnBundle(Bundle.class)
					        .cacheControl(cache)
					        .execute();
        
        assertTrue(responseActual.equalsDeep(responseTest));
        
	}
	

}

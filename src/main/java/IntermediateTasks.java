import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.util.StopWatch;

public class IntermediateTasks {

	static File f;
	static ArrayList<String> names;
	static ArrayList<Long> times;
	static double[] averages;
	
	 public static void main(String[] theArgs) throws IOException {
		 
		// Create a FHIR client
	        FhirContext fhirContext = FhirContext.forR4();
	        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
	        client.registerInterceptor(new LoggingInterceptor(false));
	        
	        //Gets file with all the names
	        f = new File("names.txt");
	        
	        //Reads all the names in the file and stores it as an arrayList
	        names = (ArrayList<String>) Files.readAllLines(Paths.get(f.getAbsolutePath()));
	        
	        //This array list will hold the response time for each of the 20 responses
	        times = new ArrayList<>();
	        
	        //This array will hold the average for each of the three loops
	        averages = new double[3];
	        
	        //This StopWatch will be used to record the time taken for each response.
	        StopWatch stopwatch = new StopWatch();
	        
	        //This CacheControlDirective will be used to disable caching during the third loop.
	        CacheControlDirective cache = new CacheControlDirective();
	        
	        //This for loop will result in the 20 queries being repeated 3 times
	        for(int i = 0; i < 3; i++) {
	        	
	        	//This For Each loop will make a query for each name in the file
	        	for(String name: names) {
	        		
			        stopwatch.startTask("response");
			        
			        //If we are on the third loop, caching is disabled. Otherwise, it is enabled.
			        if(i == 2) {
			        	cache.setNoCache(true);
			        	Bundle response = makeRequest(client, name, cache);
			        }
			        else {
			        	cache.setNoCache(false);
			        	Bundle response = makeRequest(client, name, cache);
			        }
			      
			        stopwatch.endCurrentTask();
			        
			        //The time recorded by the stopwatch is added to the times array list
			        times.add(stopwatch.getMillis());
			        stopwatch.restart();
		        }
		        
	        	//The average time of the 20 responses is calculated and stored in the averages array 
		        averages[i] = calculateAverage(times);
	        }
	        
	        DecimalFormat df = new DecimalFormat("#.00");
	        
	        //The three averages are printed to the screen
	        System.out.println("Average response time for first loop was: " + df.format(averages[0]) + " milliseconds");
	        System.out.println("Average response time for second loop was: " + df.format(averages[1]) + " milliseconds");
	        System.out.println("Average response time for third loop was: " + df.format(averages[2]) + " milliseconds");
	 }
	 
	 //Function used to calculate average of times
	 public static double calculateAverage(ArrayList<Long> times) {
		    double average = 0;
	        long total = 0;
	        
	        if(times.size() == 0) return 0;
	        
	        for(Long time: times) {
	        	total += time;
	        }
	        
	        return total / (double)times.size();
	 }
	 
	 //Function used to make the request based on a specific name and cache
	 public static Bundle makeRequest(IGenericClient client, String name, CacheControlDirective cache) {
		 return client
	                .search()
	                .forResource("Patient")
	                .where(Patient.FAMILY.matches().value(name))
	                .returnBundle(Bundle.class)
	                .cacheControl(cache)
	                .execute();
	 }
	 
}

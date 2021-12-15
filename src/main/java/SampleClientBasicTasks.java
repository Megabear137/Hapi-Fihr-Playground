import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.utils.SnomedExpressions.Base;

public class SampleClientBasicTasks {

    public static void main(String[] theArgs) {

        // Create a FHIR client
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));

        // Search for Patient resources
        Bundle response = client
                .search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value("SMITH"))
                .returnBundle(Bundle.class)
                .execute();
        
        //Store the entries in the database as a List
        List<org.hl7.fhir.r4.model.Base> entries = response.getChildByName("entry").getValues();
        
        //ArrayList that will hold all the names along with birth dates
        ArrayList<String> names = new ArrayList<>();
        
        System.out.println();
        
        //This for loop will iterate through all the users in the entries list.
        for(int i = 0; i < entries.size(); i++) {
        	
        	//Retrieves the resource from the current entry
        	org.hl7.fhir.r4.model.Base resource = entries.get(i).getChildByName("resource").getValues().get(0);
        	
        	//Retrieves the given name of the patient from the resource entry and stores it in the names list. If there is more than 
        	//one given name, each name is stored in the names list with a comma in between, e.g. "John,Joe"
        	String[] given = resource.getChildByName("name").getValues().get(0).getChildByName("given").getValues().get(0).primitiveValue().split("\",\"");
        	names.add(i, given[0]);
        	for(int j = 1; j < given.length; j++) {
        		names.set(i, names.get(i) + "," + given[j]);
        	}
        	
        	//Retrieves the family name of the patient from the resource entry and appends it to the current entry in the names list
        	names.set(i, names.get(i) + " " + resource.getChildByName("name").getValues().get(0).getChildByName("family").getValues().get(0));
        	
        	//Retrieves the birth date of the patient from the resource entry and appends it to the current entry in the names list. If no birth date
        	//is found, the message "(No Birth Date Found)" is appended instead.
        	String birthDate = "";
        	if(resource.getChildByName("birthDate").getValues().size() == 1) {
        		birthDate = resource.getChildByName("birthDate").getValues().get(0).primitiveValue();
        		if(birthDate.contains("T")) {
        			birthDate = birthDate.substring(0, birthDate.indexOf("T"));
        		}
        		
        	}
        	else birthDate = "(No Birth Date Found)";
        	names.set(i, names.get(i) + " " + birthDate);
        		
        }
         
        //The names array list is sorted in case insensitive order by first name
        names.sort(String.CASE_INSENSITIVE_ORDER);
        
        //Each name in the names list is printed to the screen.
        for(String name: names) {
        	System.out.println(name);
        }
    }
}

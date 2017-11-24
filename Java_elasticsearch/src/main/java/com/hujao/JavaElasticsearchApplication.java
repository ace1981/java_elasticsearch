package com.hujao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JavaElasticsearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(JavaElasticsearchApplication.class, args);
		
 		int total=50000;
 		SearchTexiService service=new SearchTexiService();
	    service.recreate();
		service.AddDataToIndex("沪A", 0, 0, total);
 		double lat=30.0+Math.random();
		double lon=120.0+Math.random();
 		service.find(lat, lon, 500, 10);
	}
}

package com.hujao;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = JavaElasticsearchApplication.class)
public class FindTest {
//	@Autowired 
//	SearchTexiService service;
	@Before
	public void initData(){
		int total=50000;
		SearchTexiService service=new SearchTexiService();
		service.recreate();
		service.AddDataToIndex("沪A", 0, 0, total);
	}
	
	
	@Test
	public void test() {
		fail("Not yet implemented");
	}

}

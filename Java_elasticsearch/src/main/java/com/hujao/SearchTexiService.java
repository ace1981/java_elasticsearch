package com.hujao;

import static org.assertj.core.api.Assertions.setRemoveAssertJRelatedElementsFromStackTrace;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

 

 
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.engine.Engine.Searcher;
import org.elasticsearch.index.query.GeoDistanceRangeQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

@Service
public class SearchTexiService {

	String indexName="ace_es_search";//库名
	String indexType="taxi";//表名
	
	final Settings settings  =Settings.settingsBuilder().build();
	TransportClient client;
	
	public SearchTexiService(){
		try {
			client=TransportClient.builder().settings(settings).build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("118.190.77.10"),9300));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	XContentBuilder createMapping(){
		XContentBuilder mapping=null;
		try{
			mapping=XContentFactory.jsonBuilder().startObject()
					.startObject(indexType).startObject("properties")//表
					.startObject("num").field("type","string").endObject()
					.startObject("driver").field("type","string").endObject()
					.startObject("tel").field("type","string").endObject()
					.startObject("sex").field("type","string").endObject()
					.startObject("star").field("type","integer").endObject()
					.startObject("location").field("type","geo_point").endObject()
					.endObject().endObject();
			
		}catch(IOException e){
			e.printStackTrace();
		}
		return mapping;
	}
	public void recreate(){
		try{
		client.admin().indices().prepareDelete(indexName).execute().actionGet();
		}
		catch(Exception e){
			
		}
		createIndex();
	}
	void createIndex(){
		XContentBuilder mapping=createMapping();
		client.admin().indices().prepareCreate(indexName).execute().actionGet();
		
		
		PutMappingRequest putmappingReq=Requests.putMappingRequest(indexName).type(indexType).source(mapping);
		PutMappingResponse putmappingResp=client.admin().indices().putMapping(putmappingReq).actionGet();
		if(!putmappingResp.isAcknowledged()){
			System.out.println("无法创建index");
		}
		else{
			System.out.println("创建index成功");
		}
	}
	public Integer AddDataToIndex(String cityname,double mylat,double mylon,int count){
		List<String>jsonlist=new ArrayList<>();
		for(int i=0;i<count;i++){
			double lat=30.0+Math.random();
			double lon=120.0+Math.random();
			String cardnum=cityname+(int)(Math.random()*999999);
			String name="aaa";
			try {
				name = NickBuilder.GetNick();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String tel="180"+(long)(90000000+Math.random()*99999999);
			int star=(int)(5*Math.random())+1;
			Taxi t=new Taxi(cardnum,name,tel,"男",star,lat,lon);
			jsonlist.add(ObjToJson(t));
		}
		
		List<IndexRequest>reqlist=new ArrayList<>();
		for(int i=0;i<jsonlist.size();i++){
			IndexRequest req=client.prepareIndex(indexName,indexType).setSource(jsonlist.get(i)).request();
			reqlist.add(req);
		}
		
		BulkRequestBuilder bulkreq=client.prepareBulk();
		for(IndexRequest req:reqlist){
			bulkreq.add(req);
		}
		BulkResponse resp=bulkreq.execute().actionGet();
		if(resp.hasFailures()){
			System.out.println("创建index出错");
		}
		return bulkreq.numberOfActions();
	}
	
	String ObjToJson(Taxi taxi){
		String json=null;
		try{
			XContentBuilder build=XContentFactory.jsonBuilder();
			build.startObject()
			.field("num",taxi.getNum())
			.startObject("location")
				.field("lat",taxi.getLat())
				.field("lon",taxi.getLon())
			.endObject()
			.field("driver",taxi.getDriver())
			.field("tel",taxi.getTel())
			.field("sex",taxi.getSex())
			.field("star",taxi.getStar())
			.endObject();
			json=build.string();
			System.out.println(json);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return json;	
	}
	
	public void  find(double lat,double lon,int radius,int size){
		//SearchResult result=new SearchResult();
		String unit=DistanceUnit.METERS.toString();
		 
		//获取查询规则构造器
		SearchRequestBuilder srb=client.prepareSearch(indexName).setTypes(indexType);
		
		srb.setFrom(0).setSize(size);
		
		//地理坐标
		QueryBuilder qb= new GeoDistanceRangeQueryBuilder("location")
				.point(lat, lon)
		.from("0"+unit).to(radius+unit)
		.optimizeBbox("memory")
		.geoDistance(GeoDistance.PLANE);
		srb.setPostFilter(qb);
		
		//设置排序规则
		GeoDistanceSortBuilder geosort=SortBuilders.geoDistanceSort("location");
		geosort.unit(DistanceUnit.METERS);
		geosort.order(SortOrder.ASC);//按距离升序排序
		geosort.point(lat, lon);
		srb.addSort(geosort);
		//星级
		FieldSortBuilder starSort=SortBuilders.fieldSort("star");
		starSort.order(SortOrder.DESC);//星级越高 优先级越高
		srb.addSort(starSort);
		
		SearchResponse response=srb.execute().actionGet();
		
		//高亮分词
		SearchHits hits=response.getHits();
		SearchHit [] searchHits=hits.getHits();
			
		Float usetime= response.getTookInMillis()/1000f;
		System.out.println("用时："+usetime);
		 
		System.out.println("条数："+hits.getTotalHits());
		for(SearchHit hit:searchHits){
			BigDecimal geodis=new BigDecimal((double)hit.getSortValues()[0]);

			System.out.println("车牌号："+ hit.getSource().get("num"));
			System.out.println("司机："+ hit.getSource().get("driver"));
			System.out.println("电话："+ hit.getSource().get("tel"));
			System.out.println("评分："+ hit.getSource().get("star"));
			System.out.println("距离："+geodis);
		}
		
	}


}

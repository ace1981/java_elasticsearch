package com.hujao;

public class Taxi {
	String num;
	Integer star;
	Double lat;
	Double lon;
	String driver;
	String tel;
	String sex;
	public Taxi(String num,			
			String driver,
			String tel,
			String sex,
			Integer star,
			Double lat,
			Double lon){
		this.num=num;
		this.star=star;
		this.lat=lat;
		this.lon=lon;
		this.driver=driver;
		this.tel=tel;
		this.sex=sex;
	}
	public String getNum() {
		return num;
	}
	public void setNum(String num) {
		this.num = num;
	}
	public Integer getStar() {
		return star;
	}
	public void setStar(Integer star) {
		this.star = star;
	}
	public Double getLat() {
		return lat;
	}
	public void setLat(Double lat) {
		this.lat = lat;
	}
	public Double getLon() {
		return lon;
	}
	public void setLon(Double lon) {
		this.lon = lon;
	}
	public String getDriver() {
		return driver;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	
}

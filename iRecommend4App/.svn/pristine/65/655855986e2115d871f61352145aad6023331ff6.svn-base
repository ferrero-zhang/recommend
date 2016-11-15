package com.ifeng.iRecommend.kedm.util;

public class LocalInfo {
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if(Country != null && !Country.trim().equals("")){
			sb.append(Country).append("_");
		}
		if(State != null && !State.trim().equals("")){
			sb.append(State).append("_");
		}
		if(City != null && !City.trim().equals("")){
			sb.append(City).append("_");
		}
		if(SubLocality != null && !SubLocality.trim().equals("")){
			sb.append(SubLocality).append("_");
		}
		if(Street != null && !Street.trim().equals("")){
			sb.append(Street).append("_");
		}
		if(Name != null && !Name.trim().equals("")){
			sb.append(Name);
		}
		String res = sb.toString();
		if(res.endsWith("_")){
			res = res.substring(0, res.length()-1);
		}
		return res;
	}
	private String Country;//国家名称
	private String State;//省份、州
	private String City;//城市名称
	private String SubLocality;//区名
	private String Street;//街道完整名称
	private String Name;//建筑名称
	public String getCountry() {
		return Country;
	}
	public void setCountry(String country) {
		Country = country;
	}
	public String getState() {
		return State;
	}
	public void setState(String state) {
		State = state;
	}
	public String getCity() {
		return City;
	}
	public void setCity(String city) {
		City = city;
	}
	public String getSublocality() {
		return SubLocality;
	}
	public void setSublocality(String sublocality) {
		SubLocality = sublocality;
	}
	public String getStreet() {
		return Street;
	}
	public void setStreet(String street) {
		Street = street;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}

}

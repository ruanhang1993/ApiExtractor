package cn.edu.fudan.se.apiChangeExtractor.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.Apichange;

public class Transition {

	public Map<String,String> renameMap = new HashMap<String,String>();  
	public List<Apichange> apichangeList = new ArrayList<>();
	
	public Map<String, String> getRenameMap() {
		return renameMap;
	}
	public void setRenameMap(Map<String, String> renameMap) {
		this.renameMap = renameMap;
	}
	public List<Apichange> getApichangeList() {
		return apichangeList;
	}
	public void setApichangeList(List<Apichange> apichangeList) {
		this.apichangeList = apichangeList;
	}


	
}

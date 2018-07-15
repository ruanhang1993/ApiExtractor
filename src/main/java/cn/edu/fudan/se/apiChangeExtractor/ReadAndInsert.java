package cn.edu.fudan.se.apiChangeExtractor;

import java.util.List;

import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.Apichange;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.dao.ApichangeDao;

public class ReadAndInsert {
	public void fix(){
		List<Apichange> changes = ApichangeDao.getInstance().selectAllApiChangeTest();
		for(Apichange a : changes){
			ApichangeDao.getInstance().insertApichangeFix(a);
		}
	}
	public static void main(String[] args){
		ReadAndInsert i = new ReadAndInsert();
		i.fix();
	}
}

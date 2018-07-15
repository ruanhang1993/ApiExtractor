package cn.edu.fudan.se.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cn.edu.fudan.se.apiChangeExtractor.RepeatCounter;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.Apichange;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.dao.ApichangeDao;

public class JYTest {
	@Test
	public void test(){
		ApichangeDao dao = new ApichangeDao();
		List<Apichange> list = new ArrayList<Apichange>();
//		Apichange apichange = new Apichange();
//		apichange.setRepositoryId(1);
//		apichange.setWebsite("website");
//		apichange.setOldFileName("oldFileName");
//		apichange.setNewFileName("newFileName");
//		apichange.setOldLineNumber(10);
//		apichange.setNewLineNumber(10);
//		apichange.setChangeType("changeType");
//		apichange.setOldContent("oldContent");
//		apichange.setNewContent("newContent");
//		apichange.setOldMI("oldMI");
//		apichange.setNewMI("newMI");
//		apichange.setOldCompleteClassName("oldCompleteClassName");
//		apichange.setNewCompleteClassName("newCompleteClassName");
//		apichange.setOldReceiverName("oldReceiverName");
//		apichange.setNewReceiverName("newReceiverName");
//		apichange.setOldMethodName("''");
//		apichange.setNewMethodName("newMethodName");
//		apichange.setOldParameterName("oldParameterName");
//		apichange.setNewParameterName("newParameterName");
//		apichange.setOldParameterNum(2);
//		apichange.setNewParameterNum(2);
//		apichange.setOldParameterType("oldParameterType");
//		apichange.setNewParameterType("newParameterType");
//		apichange.setParameterPosition("parameterPosition");
//		list.add(apichange);
//		list.add(new Apichange());
		dao.insertApichangeList(list);
//		RepeatCounter rc = new RepeatCounter();
	}
}

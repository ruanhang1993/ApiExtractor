package cn.edu.fudan.se.test;

import java.io.File;
import java.util.Map;

import org.junit.Test;

import cn.edu.fudan.se.apiChangeExtractor.ApiChangeExtractor;
import cn.edu.fudan.se.apiChangeExtractor.bean.JdkSequence;
import cn.edu.fudan.se.apiChangeExtractor.bean.MethodCall;

public class ApiChangeTest {
	String repositoryPath1 = "D:/javaee/parser/ApiChangeExtractor";
	String repositoryPath2 = "D:/github/ChangeExtractor";
	String repositoryPath3 = "D:/github/SEDataExtractor";
	String repositoryPath4 = "D:/javaee/LykProject";
	String repositoryPath5 = "D:/github/checkstyle";
	ApiChangeExtractor apiExtractor1 = new ApiChangeExtractor(repositoryPath1,-1);
	ApiChangeExtractor apiExtractor2 = new ApiChangeExtractor(repositoryPath2,-2);
	ApiChangeExtractor apiExtractor3 = new ApiChangeExtractor(repositoryPath3,-3);
	ApiChangeExtractor apiExtractor4 = new ApiChangeExtractor(repositoryPath4,-4);
	ApiChangeExtractor apiExtractor5 = new ApiChangeExtractor(repositoryPath5,-5);
	@Test
	public void testExtractApiChange(){
//		apiExtractor2.extractApiChange();
//		apiExtractor3.extractApiChange();
	}
	@Test
	public void testConstructData(){
		Map<Integer, JdkSequence> jdkCall = apiExtractor1.constructData(new File("D:/ApiChangeExtractor.java"));
		if(jdkCall==null) return;
		int count = 0;
		for(Integer i : jdkCall.keySet()){
			count++;
			JdkSequence j = jdkCall.get(i);
			System.out.println(i+"//"+j.getStmt());
			for(MethodCall s: j.getApiList()){
				System.out.print("|| c="+s.getCompleteClassName()+" m="+s.getMethodName()+" p="+s.getParameter());
			}
			System.out.println();
		}
		System.out.println("count : "+count);
	}
	
	@Test
	public void testExtractApiChangeByDiff(){
		apiExtractor1.extractApiChangeByDiff();
		apiExtractor2.extractApiChangeByDiff();
		apiExtractor3.extractApiChangeByDiff();
		apiExtractor4.extractApiChangeByDiff();
		apiExtractor5.extractApiChangeByDiff();
	}
	
	@Test
	public void testExtractApiChangeByDiffAfterCommit(){
		apiExtractor5.extractApiChangeByDiffAfterCommit("69d371c442e74bd03ff56a0f4cdb220599c0167a");
	}
}


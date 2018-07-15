package cn.edu.fudan.se.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import cn.edu.fudan.se.apiChangeExtractor.GumTreeExtractor;
import cn.edu.fudan.se.apiChangeExtractor.MyThreadPool;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.ExistRepository;
import cn.edu.fudan.se.apiChangeExtractor.task.ExistRepositoryTask;

public class MainTest {

	@Test
	public void test(){
		startByExist();
	}
	public void startByExist(){
		ExistRepository r1 = new ExistRepository();
		r1.setId(-1);
		r1.setRepositoryId(-3);
		r1.setAddress("D:\\github\\hprose-java");
		r1.setWebsite("test");
		GumTreeExtractor extractor = new GumTreeExtractor(r1);
		extractor.extractActions();
		extractor.clearSource();
	}

}

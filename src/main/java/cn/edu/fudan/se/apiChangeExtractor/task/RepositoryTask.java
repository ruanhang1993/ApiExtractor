package cn.edu.fudan.se.apiChangeExtractor.task;

import java.util.concurrent.Callable;

import cn.edu.fudan.se.apiChangeExtractor.ApiChangeExtractor;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.Repository;

public class RepositoryTask implements Callable<String> {
	private Repository repository;
	
	public RepositoryTask(){
	}
	
	public RepositoryTask(Repository repository){
		this.repository = repository;
	}
	
	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	@Override
	public String call() throws Exception {
		ApiChangeExtractor extractor = new ApiChangeExtractor(repository);
		extractor.extractApiChangeByDiff();
		StringBuilder builder = new StringBuilder();
		builder.append(repository.getRepositoryId());
		return builder.toString();
	}

}

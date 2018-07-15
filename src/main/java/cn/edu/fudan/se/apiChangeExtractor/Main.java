package cn.edu.fudan.se.apiChangeExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.apiChangeExtractor.evaluation.ProjectInfoParser;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.Apichange;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.ExistRepository;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.ProjectInfo;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.Repository;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.dao.ApichangeDao;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.dao.ExistRepositoryDao;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.dao.ProjectInfoDao;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.dao.RepositoryDao;
import cn.edu.fudan.se.apiChangeExtractor.task.ExistRepositoryTask;
import cn.edu.fudan.se.apiChangeExtractor.task.GumTreeParseTask;
import cn.edu.fudan.se.apiChangeExtractor.task.NotBugTask;
import cn.edu.fudan.se.apiChangeExtractor.task.RepositoryExistTask;
import cn.edu.fudan.se.apiChangeExtractor.task.RepositoryTask;
import cn.edu.fudan.se.apiChangeExtractor.util.FileUtils;
import cn.edu.fudan.se.apiChangeExtractor.util.PathUtils;

public class Main {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	private LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
	private ExecutorService service = new MyThreadPool(4, 4, 0, TimeUnit.MINUTES, queue);

	private RepositoryDao dao = new RepositoryDao();
	private ExistRepositoryDao existDao = new ExistRepositoryDao();
	
	public static void main(String[] args) {
		Main main = new Main();
		/* 处理逻辑 */
		if(args.length==3&&"db".equals(args[0])){
			int start = Integer.parseInt(args[1]);
			int end = Integer.parseInt(args[2]);
			main.startByDb(start, end);
		}else if(args.length==2&&"list".equals(args[0])){
			if("1".equals(args[1]))
				main.startByFile("/home/fdse/Downloads/GithubJavaRepositories/", "/home/fdse/Downloads/list");
			else if("2".equals(args[1]))
				main.startByFile("/home/fdse/Downloads/GithubJavaRepositories2/", "/home/fdse/Downloads/list2");
			else
				System.out.println("1 for list, 2 for list2");
		}else if(args.length==2&&"star".equals(args[0])){
			main.startByStar(Integer.parseInt(args[1]));
		}else if(args.length==2&&"repository".equals(args[0])){
			main.storeRepository(Integer.parseInt(args[1]));
		}else if(args.length==1&&"exist".equals(args[0])){
			main.startByExist();
		}else if(args.length==1&&"high".equals(args[0])){
			main.startByHigh();
		}else if(args.length==1&&"jy".equals(args[0])){
			main.startJY();
		}else if(args.length==1&&"test".equals(args[0])){
			main.startCloc();
		}else if(args.length==1&&"notbug".equals(args[0])){
			main.startNotBug();
		}else if(args.length==1&&"notrun".equals(args[0])){
			main.startNotRun();
		}else{
			System.out.println("There are 10 ways to run:");
			System.out.println("1. java -jar apichange.jar db startId endId");
			System.out.println("2. java -jar apichange.jar list 1(or 2)");
			System.out.println("3. java -jar apichange.jar star starNum");
			System.out.println("4. java -jar apichange.jar repository starNum");
			System.out.println("5. java -jar apichange.jar exist");
			System.out.println("6. java -jar apichange.jar jy");
			System.out.println("7. java -jar apichange.jar high");
			System.out.println("8. java -jar apichange.jar test");
			System.out.println("9. java -jar apichange.jar notbug");
			System.out.println("10. java -jar apichange.jar notrun");
		}
	}
	/* 测试cloc */
	public void startCloc(){
		List<ExistRepository> list = getHighData();
		for(ExistRepository temp: list){
			ProjectInfoParser parser = new ProjectInfoParser(temp);
			parser.parse();
			parser.updateMoreFive();
		}
	}
	
	
	/* 统计import static */
	public void startImport(){
		List<ExistRepository> list = getCodeOver(1000);
		int result = 0;
		for(ExistRepository temp: list){
			File dict = new File(temp.getAddress());
			if(dict.isDirectory()){
				int dictNum = getDictNum(dict);
				result+=dictNum;
				System.out.println(dictNum+" : "+temp.getAddress());
			}else{
				System.out.println("Error: "+temp.getAddress());
			}
		}
		System.out.println("Total : "+result);
	}
	private int getDictNum(File f){
		File[] files = f.listFiles();
		int res = 0;
		for(File file: files){
			if(file.isDirectory()){
				res += getDictNum(file);
			}else if(file.isFile() && file.getAbsolutePath().endsWith(".java")){
				res += totalImportStatic(file);
			}
		}
		return res;
	}
	private int totalImportStatic(File file){
		int res = 0;
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))){
			String line;
			while((line=reader.readLine())!=null){
				if(line.startsWith("package")|| "".equals(line.trim())) continue;
				if(line.startsWith("import static")) res++;
				if(!line.startsWith("import ")) break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	@Test
	public void startJY(){
		//过滤部分
		List<Apichange> apichanges = ApichangeDao.getInstance().selectAllParameterNum();
		Map<Integer, Set<String>> map = new HashMap<>();
		for(Apichange apichange : apichanges){
			int id = apichange.getRepositoryId();
			if(map.get(id)==null) 
				map.put(id, new HashSet<>());
			map.get(id).add(apichange.getCommitId());
		}
		
		for(Integer id : map.keySet()){
			ExistRepository e = ExistRepositoryDao.getInstance().selectByRepositoryId(id);
			e.setAddress(PathUtils.changeHighWebsite2Path(e.getWebsite()));
			e.setRepositoryId(e.getId());
			new FixExtractor(e,map.get(id)).extractActionsFix();
		}
	}
	
	/* 从文件提供的路径读  */
	public void startByFile(String rootPath, String listName){
		List<Repository> list = getDataByFile(rootPath, listName);
		try {
			extractRepositoriesByGumTree(list);
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn(e.getMessage());
		}	
		shutdownMain();
	}
	
	/* 从数据库每次1000个读，从start到end */
	public void startByDb(int start, int end){
		final int len = 1000;
		List<Repository> list = getData(start, Math.min(end, start+len));
		while(list!=null&&list.size()>0&&start<=end){
			try {
				extractRepositoriesByGumTree(list);
			} catch (Exception e) {
				e.printStackTrace();
				logger.warn(e.getMessage());
			}
			start+=len;
			list = getData(start, Math.min(end, start+len));
		}
		shutdownMain();
	}
	
	/* 从数据库读星大于stars的项目 */
	public void startByStar(int stars){
		List<Repository> list = getData(stars);
		try {
			extractRepositoriesByGumTree(list);
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn(e.getMessage());
		}
		shutdownMain();
	}
	
	/* 从existrepository读 */
	public void startByExist(){
		List<ExistRepository> list = getExistData();
		try {
			extractExistRepositoriesByGumTree(list);
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn(e.getMessage());
		}
		shutdownMain();
	}
	
	/* 从highqualityrepositry读 */
	public void startByHigh(){
		List<ExistRepository> list = getCodeOver(1000);
		try {
			extractExistRepositoriesByGumTree(list);
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn(e.getMessage());
		}
		shutdownMain();
	}
	/* 从highqualityrepositry读 */
	public void startNotBug(){
		List<ExistRepository> list = getHighData();
		try {
			extractNotBugByGumTree(list);
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn(e.getMessage());
		}
		shutdownMain();
	}
	/* 从highqualityrepositry读 */
	public void startNotRun(){
		List<ExistRepository> list = getNotRunData();
		try {
			extractExistRepositoriesByGumTree(list);
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn(e.getMessage());
		}
		shutdownMain();
	}
	
	/* 向existrepository存数据 */
	public void storeRepository(int stars){
		List<Repository> list = getData(stars);
		try {
			searchRepositoryExist(list);
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn(e.getMessage());
		}
		shutdownMain();
	}
	
	/* 结束线程池 */
	public void shutdownMain(){
		service.shutdown();
	}
	
	/* highqualityrepositry表的数据 */
	private List<ExistRepository> getNotRunData(){
		List<ExistRepository>  list = existDao.selectNotRun();
		for(ExistRepository e : list){
			e.setAddress(PathUtils.changeHighWebsite2Path(e.getWebsite()));
			e.setRepositoryId(e.getId());
		}
		return list;
	}
	/* highqualityrepositry表的数据 */
	private List<ExistRepository> getHighData(){
		List<ExistRepository>  list = existDao.selectHigh();
		for(ExistRepository e : list){
			e.setAddress(PathUtils.changeHighWebsite2Path(e.getWebsite()));
			e.setRepositoryId(e.getId());
		}
		return list;
	}
	/* existrepository表的数据 */
	private List<ExistRepository> getExistData(){
		return existDao.selectAll();
	}
	/* 大于stars星的数据 */
	private List<Repository> getData(int stars) {
		return dao.selectByStar(stars);
	}
	/* start至end的数据 */
	private List<Repository> getData(int start, int end){
		return dao.selectInScope(start, end);
	}
	/* 文件读的数据 */
	private List<Repository> getDataByFile(String path, String name){
		List<String> pathList = FileUtils.getPathByFile(path, name);
		List<Repository> list = new ArrayList<>(); 
		for(int i = 0; i < pathList.size(); i++){
			list.add(new Repository(i, pathList.get(i)));
		}
		return list;
	}
	
	/* 的数据 */
	private List<ExistRepository> getCodeOver(int over){
		List<ExistRepository>  list = new ArrayList<ExistRepository>();
		List<ProjectInfo> temp = (new ProjectInfoDao()).selectCodeOver(over);
		for(ProjectInfo p : temp){
			ExistRepository e = new ExistRepository();
			e.setAddress(PathUtils.changeHighWebsite2Path(p.getWebsite()));
			e.setRepositoryId(p.getRepositoryId());
			e.setWebsite(p.getWebsite());
			list.add(e);
		}
		return list;
	}
	
	/* 测试数据 */
	public List<Repository> getTestData(){
//		String repositoryPath1 = "D:/javaee/parser/ApiChangeExtractor";
//		String repositoryPath2 = "D:/github/ChangeExtractor";
//		String repositoryPath3 = "D:/github/SEDataExtractor";
//		String repositoryPath4 = "D:/javaee/LykProject";
		String repositoryPath5 = "D:/github/checkstyle";
//		String repositoryPath6 = "D:/github/spring-framework";
//		String repositoryPath7 = "D:/github/h2o-3";
//		String repositoryPath8 = "D:/repository/RxJava";
//		Repository repository1 = new Repository(-1, repositoryPath1);
//		Repository repository2 = new Repository(-2, repositoryPath2);
//		Repository repository3 = new Repository(-3, repositoryPath3);
//		Repository repository4 = new Repository(-4, repositoryPath4);
		Repository repository5 = new Repository(-5, repositoryPath5);
//		Repository repository6 = new Repository(-6, repositoryPath6);
//		Repository repository7 = new Repository(-7, repositoryPath7);
//		Repository repository8 = new Repository(-8, repositoryPath8);
		
		List<Repository> list = new ArrayList<>(); 
//		list.add(repository1);
//		list.add(repository2);
//		list.add(repository3);
//		list.add(repository4);
		list.add(repository5);
//		list.add(repository6);
//		list.add(repository7);
//		list.add(repository8);
		return list;
	}
	
	/* 
	 * 下列方法向线程池提交不同任务 
	 */
	public void extractRepositories(List<Repository> list){
		for(Repository r : list){
			RepositoryTask task = new RepositoryTask(r);
			service.submit(task);
		}
		service.shutdown();
	}
	public void extractRepositoriesInLine(List<Repository> list){
		for(Repository r : list){
			ApiChangeExtractor a = new ApiChangeExtractor(r);
			a.extractApiChangeByDiff();
		}
	}
	public void extractRepositoriesByGumTree(List<Repository> list){
		for(Repository r : list){
			GumTreeParseTask task = new GumTreeParseTask(r);
			service.submit(task);
		}
	}
	public void extractExistRepositoriesByGumTree(List<ExistRepository> list){
		for(ExistRepository r : list){
			ExistRepositoryTask task = new ExistRepositoryTask(r);
			service.submit(task);
		}
	}
	public void extractNotBugByGumTree(List<ExistRepository> list){
		for(ExistRepository r : list){
			NotBugTask task = new NotBugTask(r);
			service.submit(task);
		}
	}
	public void searchRepositoryExist(List<Repository> list){
		for(Repository r : list){
			RepositoryExistTask task = new RepositoryExistTask(r);
			service.submit(task);
		}
	}
}

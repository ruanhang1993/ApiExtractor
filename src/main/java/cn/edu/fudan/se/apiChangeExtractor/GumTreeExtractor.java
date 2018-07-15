package cn.edu.fudan.se.apiChangeExtractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;

import cn.edu.fudan.se.apiChangeExtractor.PatternMatcher.ChangeType;
import cn.edu.fudan.se.apiChangeExtractor.bean.ChangeFile;
import cn.edu.fudan.se.apiChangeExtractor.bean.Transition;
import cn.edu.fudan.se.apiChangeExtractor.evaluation.ETimer;
import cn.edu.fudan.se.apiChangeExtractor.gitReader.GitReader;
import cn.edu.fudan.se.apiChangeExtractor.gumtreeParser.GumTreeDiffParser;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.Apichange;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.ExistRepository;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.ProjectInfo;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.Repository;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.dao.ApichangeDao;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.dao.ProjectInfoDao;
import cn.edu.fudan.se.apiChangeExtractor.util.FileUtils;
import cn.edu.fudan.se.apiChangeExtractor.util.PathUtils;

public class GumTreeExtractor {
	private static final Logger logger = LoggerFactory.getLogger(GumTreeExtractor.class);
	
	private GitReader gitReader;
	private int repositoryId;
	private String repositoryPath;
	private String webRoot;
	
	public GumTreeExtractor(String path, int repositoryId){
		repositoryPath = path;
		gitReader = new GitReader(repositoryPath);
		try {
			gitReader.init();
		} catch (IOException e) {
			gitReader=null;
		}
		this.repositoryId = repositoryId;
		this.webRoot = null;
	}
	public GumTreeExtractor(Repository repository){
		repositoryPath = repository.getAddress();
		gitReader = new GitReader(repositoryPath);
		try {
			gitReader.init();
		} catch (IOException e) {
			gitReader=null;
		}
		this.repositoryId = repository.getRepositoryId();
		this.webRoot = repository.getWebsite()+"/commit/";
	}
	public GumTreeExtractor(ExistRepository repository){
		repositoryPath = repository.getAddress();
		gitReader = new GitReader(repositoryPath);
		try {
			gitReader.init();
		} catch (IOException e) {
			gitReader=null;
		}
		this.repositoryId = repository.getRepositoryId();
		this.webRoot = repository.getWebsite()+"/commit/";
	}
	
	public void extractActions(){
		//建立5个计时器
		System.out.println("gumtree parser start");
		ETimer filterTimer = new ETimer();
		ETimer diffTimer = new ETimer();
		ETimer matchTimer = new ETimer();
		ETimer countTimer = new ETimer();
		ETimer totalTimer = new ETimer();
		int usedCommits = 0;
		
		if(gitReader==null){
			logger.warn(repositoryId + " : " + repositoryPath +" repository not found!");
			return;
		}
		
		totalTimer.startTimer();
		
		filterTimer.startTimer();
		List<RevCommit> commits = gitReader.getCommitsAboutBug();
//		List<RevCommit> commits = gitReader.getCommitsAboutSecurity();
		filterTimer.endTimer();
		if(commits==null){
			logger.warn(repositoryId + " : " + repositoryPath +" commits not found!");
			return;
		}
		
		String userDirPath = System.getProperty("user.dir");
		String tempDirPath = userDirPath + "/" + UUID.randomUUID().toString();
		File tempDir = new File(tempDirPath);
		tempDir.mkdirs();
		
		for(int i = 0; i < commits.size(); i++){
			if(commits.get(i).getParents().length==0) continue;
//			System.out.println("commit id "+i + " and size = " + commits.size());
			List<ChangeFile> changeFiles = gitReader.getChangeFilesId(commits.get(i));
			if(changeFiles.size()>0) usedCommits++;
			for(ChangeFile changeFile : changeFiles){
				//读取新旧版本文件
				byte[] newContent = gitReader.getFileByObjectId(true,changeFile.getNewBlobId());
				byte[] oldContent = gitReader.getFileByObjectId(false,changeFile.getOldBlobId());
				String randomString = PathUtils.getUnitName(changeFile.getNewPath());
				File newFile = FileUtils.writeBytesToFile(newContent, tempDirPath, randomString + ".v1");
				File oldFile = FileUtils.writeBytesToFile(oldContent, tempDirPath, randomString + ".v2");
				
				//删除过大文件
				if(newFile.length()/1048576>1) continue;
				
				diffTimer.startTimer();
				GumTreeDiffParser diff = new GumTreeDiffParser(oldFile, newFile);
				//GumTree分析出问题，跳过文件
				try{
					diff.init();
				}catch(Exception e){
//					System.out.println(repositoryId+"|"+changeFile.getCommitId() +" : "+changeFile.getNewPath() + " error");
					newFile.delete();
					oldFile.delete();
					continue;
				}
				diffTimer.endTimer();
				
				//分析Action
				matchTimer.startTimer();
				List<Action> actions = diff.getActions();
				ActionParser parser = new ActionParser(commits.get(i), diff, webRoot, changeFile, repositoryId, countTimer);
				Transition tran = new Transition();
				
				// 通过新的数据结构进行存储
				
				for(Action a:actions)
				{										
					tran = parser.parseOneAction(a,tran);		    
				}
				
				//处理合并  如果前后两个都是改参数并且节点相同 那么合并把位置合并

//				if(tran.getApichangeList().size() == 0)
//					System.out.print("0");
//				if(tran.getApichangeList().size() != 0)
//					System.out.println(tran.getApichangeList());
				
				
				
				for(int j=0;j<tran.getApichangeList().size();j++)
				{
				    if(j+1<tran.getApichangeList().size())
				    {
				    	Apichange currentApichange = tran.getApichangeList().get(j);
				    	Apichange nextApichange = tran.getApichangeList().get(j+1);				    					    	
				    	if(ChangeType.CHANGE_PAREMETER.toString().equals(currentApichange.getChangeType()) 
			    	    			&&ChangeType.CHANGE_PAREMETER.toString().equals(nextApichange.getChangeType()))
				    	{
				    		//判定是不是同一个
				    		if(currentApichange.getAction().getNode().getParent() 
				    				== nextApichange.getAction().getNode().getParent())
				    		{
				    			String realPosition = currentApichange.getParameterPosition();
				    			realPosition = realPosition +","+nextApichange.getParameterPosition();			    			
				    			currentApichange.setParameterPosition(realPosition);
				    			tran.getApichangeList().remove(j+1);
				    			j = j-1;				    			
				    		}			    				    	
				    	}		 	    	
				    }
				}
				
				//入库
				ApichangeDao dao = new ApichangeDao();
				if(!tran.getApichangeList().isEmpty())
				{
					dao.insertApichangeList(tran.getApichangeList());	
					System.out.println("insert success");
				}
				
				matchTimer.endTimer();
				
				newFile.delete();
				oldFile.delete();
			}
		}
		tempDir.delete();
		totalTimer.endTimer();
		
//		ProjectInfo info = new ProjectInfo();
//		info.setRepositoryId(repositoryId);
//		info.setAordBugCommits(usedCommits);
//		info.setFilterTime((int)filterTimer.getTotalTime());
//		info.setDiffTime((int)diffTimer.getTotalTime());
//		info.setMatchTime((int)matchTimer.getTotalTime());
//		info.setCountTime((int)countTimer.getTotalTime());
//		info.setTotalTime((int)totalTimer.getTotalTime());
//		ProjectInfoDao.getInstance().updateProjectInfo(info);
		logger.warn("repository "+repositoryId+" end extractor.");
	}
	
	public void extractActionsNotBug(){
		if(gitReader==null){
			logger.warn(repositoryId + " : " + repositoryPath +" repository not found!");
			return;
		}
		
		List<RevCommit> commits = gitReader.getCommitsNotBug();
		if(commits==null){
			logger.warn(repositoryId + " : " + repositoryPath +" commits not found!");
			return;
		}
		
		String userDirPath = System.getProperty("user.dir");
		String tempDirPath = userDirPath + "/" + UUID.randomUUID().toString();
		File tempDir = new File(tempDirPath);
		tempDir.mkdirs();
		
		for(int i = 0; i < commits.size(); i++){
			if(commits.get(i).getParents().length==0) continue;
			
			List<ChangeFile> changeFiles = gitReader.getChangeFilesId(commits.get(i));
			for(ChangeFile changeFile : changeFiles){
				//读取新旧版本文件
				byte[] newContent = gitReader.getFileByObjectId(true,changeFile.getNewBlobId());
				byte[] oldContent = gitReader.getFileByObjectId(false,changeFile.getOldBlobId());
				String randomString = PathUtils.getUnitName(changeFile.getNewPath());
				File newFile = FileUtils.writeBytesToFile(newContent, tempDirPath, randomString + ".v1");
				File oldFile = FileUtils.writeBytesToFile(oldContent, tempDirPath, randomString + ".v2");
				
				//删除过大文件
				if(newFile.length()/1048576>1) continue;
				
				GumTreeDiffParser diff = new GumTreeDiffParser(oldFile, newFile);
				//GumTree分析出问题，跳过文件
				try{
					diff.init();
				}catch(Exception e){
//					System.out.println(repositoryId+"|"+changeFile.getCommitId() +" : "+changeFile.getNewPath() + " error");
					newFile.delete();
					oldFile.delete();
					continue;
				}
				
				//分析Action
				List<Action> actions = diff.getActions();
				ActionParser parser = new ActionParser(commits.get(i), diff, webRoot, changeFile, repositoryId, null);
				Transition tran = new Transition();
				for(Action a:actions)
				{
					// 数据结构接收 parse的时候传入
					// 如果update是个声明的话
					// 之后pattern检测到了对其进行过滤
					
					tran = parser.parseOneActionNotBug(a,tran);
				}
				
				newFile.delete();
				oldFile.delete();
			}
		}
		tempDir.delete();
		
		logger.warn("[not bug]repository "+repositoryId+" end extractor.");
	}
	public void clearSource(){
		if(gitReader!=null) gitReader.close();
	}
}

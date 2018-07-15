package cn.edu.fudan.se.apiChangeExtractor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.apiChangeExtractor.bean.ChangeFile;
import cn.edu.fudan.se.apiChangeExtractor.bean.Transition;
import cn.edu.fudan.se.apiChangeExtractor.gitReader.GitReader;
import cn.edu.fudan.se.apiChangeExtractor.gumtreeParser.GumTreeDiffParser;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.ExistRepository;
import cn.edu.fudan.se.apiChangeExtractor.util.FileUtils;
import cn.edu.fudan.se.apiChangeExtractor.util.PathUtils;

import com.github.gumtreediff.actions.model.Action;

public class FixExtractor {
	private static final Logger logger = LoggerFactory.getLogger(FixExtractor.class);
	
	Set<String> toDeal;
	private GitReader gitReader;
	private int repositoryId;
	private String repositoryPath;
	private String webRoot;
	
	public FixExtractor(ExistRepository repository, Set<String> toDeal){
		repositoryPath = repository.getAddress();
		gitReader = new GitReader(repositoryPath);
		try {
			gitReader.init();
		} catch (IOException e) {
			gitReader=null;
		}
		this.toDeal = toDeal;
		this.repositoryId = repository.getRepositoryId();
		this.webRoot = repository.getWebsite()+"/commit/";
	}
	
	public void extractActionsFix(){
		if(gitReader==null){
			logger.warn(repositoryId + " : " + repositoryPath +" repository not found!");
			return;
		}
		
		List<RevCommit> commits = gitReader.getCommitsFix(toDeal);
		if(commits==null){
			logger.warn(repositoryId + " : " + repositoryPath +" commits not found!");
			return;
		}
		
		String userDirPath = System.getProperty("user.dir");
		String tempDirPath = userDirPath + "/" + UUID.randomUUID().toString();
		File tempDir = new File(tempDirPath);
		tempDir.mkdirs();
		if(this.repositoryId == 1)
			System.out.println("commit size: "+commits.size());
		for(int i = 0; i < commits.size(); i++){
			if(commits.get(i).getParents().length==0) continue;
			
			List<ChangeFile> changeFiles = gitReader.getChangeFilesId(commits.get(i));
			for(ChangeFile changeFile : changeFiles){
				if("src/test/java/io/reactivex/XFlatMapTest.java".equals(changeFile.getOldPath()))
					System.out.println("src/test/java/io/reactivex/XFlatMapTest.java");
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
					newFile.delete();
					oldFile.delete();
					continue;
				}
				
				//分析Action
				List<Action> actions = diff.getActions();
				ActionParser parser = new ActionParser(commits.get(i), diff, webRoot, changeFile, repositoryId, null);
				Transition rl = new Transition();
				if(this.repositoryId == 1)
					System.out.println("commit order: "+i);
				for(Action a:actions)
				{
					// 数据结构接收 parse的时候传入
					// 如果update是个声明的话
					// 之后pattern检测到了对其进行过滤
					
					rl = parser.fixChangeParameter(a,rl);
				}
				
				newFile.delete();
				oldFile.delete();
			}
		}
		tempDir.delete();
		
		logger.warn("[fix]repository "+repositoryId+" end extractor.");
		System.out.println("repository: "+repositoryId+" done.");
	}
	public void clearSource(){
		if(gitReader!=null) gitReader.close();
	}
}

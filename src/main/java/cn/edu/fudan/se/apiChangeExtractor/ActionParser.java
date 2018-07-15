package cn.edu.fudan.se.apiChangeExtractor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jgit.revwalk.RevCommit;

import cn.edu.fudan.se.apiChangeExtractor.PatternMatcher.ChangeType;
import cn.edu.fudan.se.apiChangeExtractor.bean.ChangeFile;
import cn.edu.fudan.se.apiChangeExtractor.bean.JdtMethodCall;
import cn.edu.fudan.se.apiChangeExtractor.bean.Transition;
import cn.edu.fudan.se.apiChangeExtractor.evaluation.ETimer;
import cn.edu.fudan.se.apiChangeExtractor.gumtreeParser.GumTreeDiffParser;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.Apichange;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.ChangeExample;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.InnerChangeExample;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.dao.ApichangeDao;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.dao.ApichangeNotBugDao;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;

public class ActionParser {
	private GumTreeDiffParser diff;
	private ApichangeDao dao;
	private ApichangeNotBugDao notBugDao;
	private String webRoot;
	private ChangeFile changeFile;
	private int repositoryId;
	private RevCommit thisCommit;
	private ETimer timer;
	
	public ActionParser(RevCommit thisCommit, GumTreeDiffParser diff, String webRoot, ChangeFile changeFile, int repositoryId, ETimer timer){
		this.thisCommit = thisCommit;
		this.diff = diff;
		dao = new ApichangeDao();
		notBugDao = new ApichangeNotBugDao();
		this.webRoot = webRoot;
		this.changeFile = changeFile;
		this.repositoryId = repositoryId;
		this.timer =timer;
	}
	public Transition parseOneAction(Action a, Transition tran){
		if(a.getNode()==null || diff.dstTC==null)
		    return tran;
		
		PatternMatcher pm = new PatternMatcher();
	    ChangeType changeType = pm.matchOneAction(a, diff.dstTC, diff, tran);
		
	    Apichange apichange = new Apichange();

		//insert record or not 数据库存储操作
		if(!ChangeType.NOT_FOUND.toString().equals(changeType.toString()))
		{
//		    System.out.println();
//		    System.out.print(changeType.toString());
			apichange.setAction(a);
			apichange.setCommitLog(thisCommit.getFullMessage());
			//apichange的赋值
			apichange.setRepositoryId(repositoryId);
			apichange.setWebsite(webRoot+changeFile.getCommitId());
			apichange.setCommitTime(thisCommit.getCommitTime());
			apichange.setCommitId(changeFile.getCommitId());
			apichange.setParentCommitId(changeFile.getParentCommitId());
			apichange.setNewFileName(changeFile.getNewPath());
			apichange.setOldFileName(changeFile.getOldPath());
			ITree newNode = null; 
			ITree oldNode = null;	
			apichange.setChangeType(changeType.toString());  //新表改为int？
			if(a instanceof Insert)
			{
				newNode = a.getNode();
//				oldNode = diff.getMapping().getSrc(newNode);	
				
			}
			if(a instanceof Update)
			{
				oldNode = a.getNode();
				newNode = diff.getMapping().getDst(oldNode);							
			}
			if(oldNode!=null)
			{
//				System.out.print("oldNode!=null");
				apichange.setOldLineNumber(oldNode.getStartLineNumber());
				apichange.setOldContent(((Tree)oldNode).getAstNode().toString());
				String tempOldParameterName = new String();
				List<ITree> oldChildren= oldNode.getParent().getChildren();
				if(oldChildren.size()>=2)	
				{
					for(int j=2;j<oldChildren.size();j++)
					{
					    tempOldParameterName = tempOldParameterName+oldChildren.get(j).getLabel();
					    if(j!=oldChildren.size()-1)
					    	tempOldParameterName = tempOldParameterName +",";
					}
				}
				apichange.setOldParameterName(tempOldParameterName);
				
				JdtMethodCall oldCall = null;
				if(((Tree)oldNode.getParent()).getAstNode() instanceof MethodInvocation){	
					MethodInvocation tempMI = (MethodInvocation)((Tree)oldNode.getParent()).getAstNode();
					oldCall = diff.getJdkMethodCall(tempMI);
					apichange.setOldMI(tempMI.toString());
				}
				if(oldCall!=null)
				{
//					System.out.print("oldCall!=null");
					apichange.setOldCompleteClassName(oldCall.getInvoker());
					apichange.setOldReceiverName(((Tree)oldNode.getParent()).getChild(0).getLabel());
					apichange.setOldMethodName(oldCall.getMethodName());
					apichange.setOldParameterNum(oldCall.getParameters().size());
					apichange.setOldParameterType(oldCall.getParameterString());
					ITree parent = oldNode.getParent();
					int position  = parent.getChildPosition(oldNode);
					System.out.println("position int "+position);
					apichange.setParameterPosition(String.valueOf(position-2));
					System.out.println("position string "+String.valueOf(position-2));
				}
				else
					return tran;
			}
			if(newNode!=null)
			{
//				System.out.print("newNode!=null");
				apichange.setNewLineNumber(newNode.getStartLineNumber());//map去找
				apichange.setNewContent(((Tree)newNode).getAstNode().toString());
				String tempNewParameterName = new String();
				List<ITree> newChildren= newNode.getParent().getChildren();
				if(newChildren.size()>=2)	
				{
					for(int j=2;j<newChildren.size();j++)
					{
					    tempNewParameterName = tempNewParameterName+newChildren.get(j).getLabel();
					    if(j!=newChildren.size()-1)
					    	tempNewParameterName = tempNewParameterName +",";
					}
				}
				apichange.setNewParameterName(tempNewParameterName);
				JdtMethodCall newCall = null;
				if(((Tree)newNode.getParent()).getAstNode() instanceof MethodInvocation){
					MethodInvocation tempMI = (MethodInvocation)((Tree)newNode.getParent()).getAstNode();
					newCall = diff.getJdkMethodCall(tempMI);
					apichange.setNewMI(tempMI.toString());
				}
				if(newCall!=null)
				{
					
//					System.out.print("newCall!=null");
					apichange.setNewCompleteClassName(newCall.getInvoker());
					apichange.setNewReceiverName(((Tree)newNode.getParent()).getChild(0).getLabel());
					apichange.setNewMethodName(newCall.getMethodName());
					apichange.setNewParameterNum(newCall.getParameters().size());
					apichange.setNewParameterType(newCall.getParameterString());	
					if(newCall.isJdk())
					{
//						System.out.println("start add");
						if(apichange.getParameterPosition()==null)
						{							
							ITree parent = newNode.getParent();
							int position  = parent.getChildPosition(newNode);
							System.out.println("position int "+position);
							apichange.setParameterPosition(String.valueOf(position-2));
							System.out.println("position string "+String.valueOf(position-2));
						}
						List<Apichange> apichangeList = new ArrayList<>();
						apichangeList = tran.getApichangeList();
						apichangeList.add(apichange);
						tran.setApichangeList(apichangeList);
//						System.out.println("add sucess");
					}
				}	
			}									
		}
		return tran;
	}

	public Transition parseOneActionNotBug(Action a, Transition tran){
		if(a.getNode()==null || diff.dstTC==null)
		    return tran;
		
		PatternMatcher pm = new PatternMatcher();
	    ChangeType changeType = pm.matchOneAction(a, diff.dstTC, diff, tran);
		Apichange apichange = new Apichange();
		//insert record or not 数据库存储操作
		if(!ChangeType.NOT_FOUND.toString().equals(changeType.toString()))
		{
			apichange.setCommitLog(thisCommit.getFullMessage());
			//apichange的赋值
			apichange.setRepositoryId(repositoryId);
			apichange.setWebsite(webRoot+changeFile.getCommitId());
			apichange.setCommitId(changeFile.getCommitId());
			apichange.setParentCommitId(changeFile.getParentCommitId());
			apichange.setNewFileName(changeFile.getNewPath());
			apichange.setOldFileName(changeFile.getOldPath());
			ITree newNode = null; 
			ITree oldNode = null;	
			apichange.setChangeType(changeType.toString());  //新表改为int？
			if(a instanceof Insert)
			{
				newNode = a.getNode();
//				oldNode = diff.getMapping().getSrc(newNode);	
				
			}
			if(a instanceof Update)
			{
				oldNode = a.getNode();
				newNode = diff.getMapping().getDst(oldNode);							
			}
			if(oldNode!=null)
			{
				apichange.setOldLineNumber(oldNode.getStartLineNumber());
				apichange.setOldContent(((Tree)oldNode).getAstNode().toString());
				String tempOldParameterName = new String();
				List<ITree> oldChildren= oldNode.getParent().getChildren();
				if(oldChildren.size()>=2)	
				{
					for(int j=2;j<oldChildren.size();j++)
					{
					    tempOldParameterName = tempOldParameterName+oldChildren.get(j).getLabel();
					    if(j!=oldChildren.size()-1)
					    	tempOldParameterName = tempOldParameterName +",";
					}
				}
				apichange.setOldParameterName(tempOldParameterName);
				
				JdtMethodCall oldCall = null;
				if(((Tree)oldNode.getParent()).getAstNode() instanceof MethodInvocation){	
					MethodInvocation tempMI = (MethodInvocation)((Tree)oldNode.getParent()).getAstNode();
					oldCall = diff.getJdkMethodCall(tempMI);
					apichange.setOldMI(tempMI.toString());
				}
				if(oldCall!=null)
				{
					apichange.setOldCompleteClassName(oldCall.getInvoker());
					apichange.setOldMethodName(oldCall.getMethodName());
					apichange.setOldParameterNum(oldCall.getParameters().size());
					apichange.setOldParameterType(oldCall.getParameterString());
				}
				else
					return tran;
			}
			if(newNode!=null)
			{
				apichange.setNewLineNumber(newNode.getStartLineNumber());//map去找
				apichange.setNewContent(((Tree)newNode).getAstNode().toString());
				String tempNewParameterName = new String();
				List<ITree> newChildren= newNode.getParent().getChildren();
				if(newChildren.size()>=2)	
				{
					for(int j=2;j<newChildren.size();j++)
					{
					    tempNewParameterName = tempNewParameterName+newChildren.get(j).getLabel();
					    if(j!=newChildren.size()-1)
					    	tempNewParameterName = tempNewParameterName +",";
					}
				}
				apichange.setNewParameterName(tempNewParameterName);
				JdtMethodCall newCall = null;
				if(((Tree)newNode.getParent()).getAstNode() instanceof MethodInvocation){
					MethodInvocation tempMI = (MethodInvocation)((Tree)newNode.getParent()).getAstNode();
					newCall = diff.getJdkMethodCall(tempMI);
					apichange.setNewMI(tempMI.toString());
				}
				if(newCall!=null)
				{
					
					apichange.setNewCompleteClassName(newCall.getInvoker());							
					apichange.setNewMethodName(newCall.getMethodName());
					apichange.setNewParameterNum(newCall.getParameters().size());
					apichange.setNewParameterType(newCall.getParameterString());	
					if(newCall.isJdk())
					{

						notBugDao.insertOneApichange(apichange);
					}
				}	
			}									
		}
		return tran;
	}
	
	public Transition fixChangeParameter(Action a, Transition tran){
		if(a.getNode()==null || diff.dstTC==null)
		    return tran;
		
		PatternMatcher pm = new PatternMatcher();
	    ChangeType changeType = pm.matchOneAction(a, diff.dstTC, diff, tran);
		Apichange apichange = new Apichange();
		ChangeExample example = new ChangeExample();
		InnerChangeExample innerExample = new InnerChangeExample();
		//insert record or not 数据库存储操作
		if("src/test/java/io/reactivex/XFlatMapTest.java".equals(changeFile.getOldPath()))
			System.out.println("change type: "+changeType.toString());
		if(ChangeType.CHANGE_PAREMETER.toString().equals(changeType.toString()))
		{
			apichange.setCommitLog(thisCommit.getFullMessage());
			//apichange的赋值
			apichange.setRepositoryId(repositoryId);
			apichange.setWebsite(webRoot+changeFile.getCommitId());
			apichange.setCommitId(changeFile.getCommitId());
			apichange.setParentCommitId(changeFile.getParentCommitId());
			apichange.setNewFileName(changeFile.getNewPath());
			apichange.setOldFileName(changeFile.getOldPath());
			ITree newNode = null; 
			ITree oldNode = null;	
			apichange.setChangeType(changeType.toString());  //新表改为int？
			if(a instanceof Insert)
			{
				newNode = a.getNode();
//				oldNode = diff.getMapping().getSrc(newNode);	
				
			}
			if(a instanceof Update)
			{
				oldNode = a.getNode();
				newNode = diff.getMapping().getDst(oldNode);							
			}
			if(oldNode!=null)
			{
				apichange.setOldLineNumber(oldNode.getStartLineNumber());
				apichange.setOldContent(((Tree)oldNode).getAstNode().toString());
				String tempOldParameterName = new String();
				List<ITree> oldChildren= oldNode.getParent().getChildren();
				if(oldChildren.size()>=2)	
				{
					for(int j=2;j<oldChildren.size();j++)
					{
					    tempOldParameterName = tempOldParameterName+oldChildren.get(j).getLabel();
					    if(j!=oldChildren.size()-1)
					    	tempOldParameterName = tempOldParameterName +",";
					}
				}
				apichange.setOldParameterName(tempOldParameterName);
				
				JdtMethodCall oldCall = null;
				if(((Tree)oldNode.getParent()).getAstNode() instanceof MethodInvocation){	
					MethodInvocation tempMI = (MethodInvocation)((Tree)oldNode.getParent()).getAstNode();
					oldCall = diff.getJdkMethodCall(tempMI);
					apichange.setOldMI(tempMI.toString());
				}
				if(oldCall!=null)
				{
					apichange.setOldCompleteClassName(oldCall.getInvoker());
					apichange.setOldMethodName(oldCall.getMethodName());
					apichange.setOldParameterNum(oldCall.getParameters().size());
					apichange.setOldParameterType(oldCall.getParameterString());
					ITree parent = oldNode.getParent();
					int position  = parent.getChildPosition(oldNode);
					System.out.println("position int"+position);
					apichange.setParameterPosition(String.valueOf(position-2));
					System.out.println("position string"+String.valueOf(position-2));
//					System.out.println(position);
				}
				else
					return tran;
			}
			if(newNode!=null)
			{
				apichange.setNewLineNumber(newNode.getStartLineNumber());//map去找
				apichange.setNewContent(((Tree)newNode).getAstNode().toString());
				String tempNewParameterName = new String();
				List<ITree> newChildren= newNode.getParent().getChildren();
				if(newChildren.size()>=2)	
				{
					for(int j=2;j<newChildren.size();j++)
					{
					    tempNewParameterName = tempNewParameterName+newChildren.get(j).getLabel();
					    if(j!=newChildren.size()-1)
					    	tempNewParameterName = tempNewParameterName +",";
					}
				}
				apichange.setNewParameterName(tempNewParameterName);
				JdtMethodCall newCall = null;
				if(((Tree)newNode.getParent()).getAstNode() instanceof MethodInvocation){
					MethodInvocation tempMI = (MethodInvocation)((Tree)newNode.getParent()).getAstNode();
					newCall = diff.getJdkMethodCall(tempMI);
					apichange.setNewMI(tempMI.toString());
				}
				if(newCall!=null)
				{
					
					apichange.setNewCompleteClassName(newCall.getInvoker());							
					apichange.setNewMethodName(newCall.getMethodName());
					apichange.setNewParameterNum(newCall.getParameters().size());
					apichange.setNewParameterType(newCall.getParameterString());	
					if(newCall.isJdk())
					{
						if(apichange.getRepositoryId()==1)
							System.out.println(apichange.toString());
						dao.updateParameterPosition(apichange);
					}
				}	
			}									
		}
		return tran;
	}
	
	
}

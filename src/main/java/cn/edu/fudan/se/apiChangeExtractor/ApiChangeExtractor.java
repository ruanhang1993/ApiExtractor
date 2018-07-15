package cn.edu.fudan.se.apiChangeExtractor;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.Node;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.ExpressionStmt;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//
//import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
//import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
//import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
//import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
//import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import cn.edu.fudan.se.apiChangeExtractor.ast.CodeTree;
import cn.edu.fudan.se.apiChangeExtractor.ast.DisplayTreeView;
import cn.edu.fudan.se.apiChangeExtractor.ast.JapaAst;
import cn.edu.fudan.se.apiChangeExtractor.ast.SimplifiedTreeCreator;
import cn.edu.fudan.se.apiChangeExtractor.ast.TreeView;
import cn.edu.fudan.se.apiChangeExtractor.ast.UserClassProcessing;
import cn.edu.fudan.se.apiChangeExtractor.bean.ChangeFile;
import cn.edu.fudan.se.apiChangeExtractor.bean.ChangeLine;
import cn.edu.fudan.se.apiChangeExtractor.bean.JdkSequence;
import cn.edu.fudan.se.apiChangeExtractor.bean.MethodCall;
import cn.edu.fudan.se.apiChangeExtractor.gitReader.GitReader;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.Apichange;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.ChangeExample;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.Repository;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.dao.ApichangeDao;
import cn.edu.fudan.se.apiChangeExtractor.util.FileUtils;

public class ApiChangeExtractor {
	private static final Logger logger = LoggerFactory.getLogger(ApiChangeExtractor.class);
	
	private GitReader gitReader;
//	private ChangeExtractor changeExactor;
	private int repositoryId;
	private ApichangeDao dao;
	
	public ApiChangeExtractor(String path, int repositoryId){
//		changeExactor = new ChangeExtractor();
		gitReader = new GitReader(path);
		try {
			gitReader.init();
		} catch (IOException e) {
			e.printStackTrace();
			gitReader=null;
		}
		this.repositoryId = repositoryId;
		dao = new ApichangeDao();
	}
	public ApiChangeExtractor(Repository repository){
//		changeExactor = new ChangeExtractor();
		gitReader = new GitReader(repository.getAddress());
		try {
			gitReader.init();
		} catch (IOException e) {
			e.printStackTrace();
			gitReader=null;
		}
		this.repositoryId = repository.getRepositoryId();
		dao = new ApichangeDao();
	}
	
	public void extractApiChangeByDiffAfterCommit(String sha){
		String userDirPath = System.getProperty("user.dir");
		String tempDirPath = userDirPath + "/" + UUID.randomUUID().toString();
		File tempDir = new File(tempDirPath);
		tempDir.mkdirs();
		List<RevCommit> commits = gitReader.getCommits();
		int count = 0;
		for(;count<commits.size(); count++){
			if(commits.get(count).getName().equals(sha))
				break;
		}
		for(;count<commits.size(); count++){
			List<Apichange> apiChangeList = new ArrayList<Apichange>();
			if(commits.get(count).getParents().length==0) continue;
			System.out.println(count+"===="+commits.get(count).getName()+"=======================================================================================================================================");
//			System.out.println(count+"----"+commits.get(count).getParent(0).getName()+"---------------------------------------------------------------------------------------------------------------------------------------");
			List<ChangeFile> changeFiles = gitReader.getChangeFiles(commits.get(count));
			for(ChangeFile changeFile : changeFiles){
				byte[] newContent = gitReader.getFileByObjectId(true,changeFile.getNewBlobId());
				byte[] oldContent = gitReader.getFileByObjectId(false,changeFile.getOldBlobId());
				String randomString = UUID.randomUUID().toString();
				File newFile = FileUtils.writeBytesToFile(newContent, tempDirPath, randomString + ".v1");
				File oldFile = FileUtils.writeBytesToFile(oldContent, tempDirPath, randomString + ".v2");
				Map<Integer, JdkSequence> newJdkCall = null;
				Map<Integer, JdkSequence> oldJdkCall = null;
				try{
					System.out.println("new ***>"+changeFile.getNewPath());
					newJdkCall = constructData(newFile);
					System.out.println("old ***>"+changeFile.getOldPath());
					oldJdkCall = constructData(oldFile);
				}catch(Exception e){
					logger.info("repository "+repositoryId+"/commit: "+commits.get(count).getName()+" debug:");
					logger.info(e.getMessage());
					e.printStackTrace();
				}
				
				for(ChangeLine line : changeFile.getChangeLines()){
					if(GitReader.ChangeType.ADD.toString().equals(line.getType())){
						matchChangeAndApi(apiChangeList, line, newJdkCall, changeFile);
					}else if(GitReader.ChangeType.DELETE.toString().equals(line.getType())){
						matchChangeAndApi(apiChangeList, line, oldJdkCall, changeFile);
					}else{
						//TODO CONTENT类型尚未处理
					}
				}
				newFile.delete();
				oldFile.delete();
			}
			if(apiChangeList.size()>0){
				dao.insertApichangeList(apiChangeList);
				for(Apichange ac: apiChangeList){
					ChangeExample example = new ChangeExample();
					List<ChangeExample> res = null;
					example.setChangeType(ac.getChangeType());
					example.setNewCompleteClassName(ac.getNewCompleteClassName());
					example.setOldCompleteClassName(ac.getOldCompleteClassName());
					example.setOldMethodName(ac.getOldMethodName());
					example.setNewMethodName(ac.getNewMethodName());
					example.setOuterRepeatNum(ac.getOuterRepeatNum());
					res = dao.selectExample(example);
					if(res == null || res.size()==0){
						dao.insertExample(example);
						System.out.println("Insert new Example");

						res = dao.selectExample(example);
						ac.setExampleId(res.get(0).getExampleId());
						dao.updateApichangeExampleId(ac);

					}
					else{
						ac.setExampleId(res.get(0).getExampleId());
						dao.updateApichangeExampleId(ac);
					}
					
					int innerRepeatNum = 0;
					int outerRepeatNum = 0;

					innerRepeatNum = dao.countInnerRepeat(ac);
					outerRepeatNum = dao.countOuterRepeat(ac);
					ac.setInnerRepeatNum(innerRepeatNum);
					ac.setOuterRepeatNum(outerRepeatNum);
					dao.updateInnerRepeat(ac);
					dao.updateOuterRepeat(ac);
					example.setOuterRepeatNum(outerRepeatNum);
					dao.updateExampleOuterRepeatNum(example);
				}
			}
		}
		tempDir.delete();
	}
	
	public void extractApiChangeByDiff(){
		System.out.println("repository "+repositoryId+" start extractor*****************************************************************************************************");
		String userDirPath = System.getProperty("user.dir");
		String tempDirPath = userDirPath + "/" + UUID.randomUUID().toString();
		File tempDir = new File(tempDirPath);
		tempDir.mkdirs();
		List<RevCommit> commits = gitReader.getCommits();
		if(commits==null) return;
		
		for(int i = 0; i < commits.size(); i++){
			List<Apichange> apiChangeList = new ArrayList<Apichange>();
			if(commits.get(i).getParents().length==0) continue;
			System.out.println(i+"===="+commits.get(i).getName()+"=======================================================================================================================================");
//			System.out.println(i+"----"+commits.get(i).getParent(0).getName()+"---------------------------------------------------------------------------------------------------------------------------------------");

			List<ChangeFile> changeFiles = gitReader.getChangeFiles(commits.get(i));
			for(ChangeFile changeFile : changeFiles){
				byte[] newContent = gitReader.getFileByObjectId(true,changeFile.getNewBlobId());
				byte[] oldContent = gitReader.getFileByObjectId(false,changeFile.getOldBlobId());
				String randomString = UUID.randomUUID().toString();
				File newFile = FileUtils.writeBytesToFile(newContent, tempDirPath, randomString + ".v1");
				File oldFile = FileUtils.writeBytesToFile(oldContent, tempDirPath, randomString + ".v2");
				
				Map<Integer, JdkSequence> newJdkCall = null;
				Map<Integer, JdkSequence> oldJdkCall = null;
				try{
//					System.out.println("new ***>"+changeFile.getNewPath());
					newJdkCall = constructData(newFile);
//					System.out.println("old ***>"+changeFile.getOldPath());
					oldJdkCall = constructData(oldFile);
				}catch(Exception e){
					logger.info("repository "+repositoryId+"/commit: "+commits.get(i).getName()+" debug:");
					logger.info(e.getMessage());
					e.printStackTrace();
				}
				
				for(ChangeLine line : changeFile.getChangeLines()){
					if(GitReader.ChangeType.ADD.toString().equals(line.getType())){
						matchChangeAndApi(apiChangeList, line, newJdkCall, changeFile);
					}else if(GitReader.ChangeType.DELETE.toString().equals(line.getType())){
						matchChangeAndApi(apiChangeList, line, oldJdkCall, changeFile);
					}else{
						//TODO CONTENT类型尚未处理
					}
				}
				newFile.delete();
				oldFile.delete();
			}
			if(apiChangeList.size()>0){
				dao.insertApichangeList(apiChangeList);
				for(Apichange ac: apiChangeList){
					ChangeExample example = new ChangeExample();
					List<ChangeExample> res = null;
					example.setChangeType(ac.getChangeType());
					example.setNewCompleteClassName(ac.getNewCompleteClassName());
					example.setOldCompleteClassName(ac.getOldCompleteClassName());
					example.setOldMethodName(ac.getOldMethodName());
					example.setNewMethodName(ac.getNewMethodName());
					example.setOuterRepeatNum(ac.getOuterRepeatNum());

					res = dao.selectExample(example);

					if(res == null || res.size()==0){
						dao.insertExample(example);
						System.out.println("Insert new Example");
						
						res = dao.selectExample(example);
						ac.setExampleId(res.get(0).getExampleId());
						dao.updateApichangeExampleId(ac);
					}
					else{
						ac.setExampleId(res.get(0).getExampleId());
						dao.updateApichangeExampleId(ac);
					}
					
					int innerRepeatNum = 0;
					int outerRepeatNum = 0;

					innerRepeatNum = dao.countInnerRepeat(ac);
					outerRepeatNum = dao.countOuterRepeat(ac);
					ac.setInnerRepeatNum(innerRepeatNum);
					ac.setOuterRepeatNum(outerRepeatNum);
					dao.updateInnerRepeat(ac);
					dao.updateOuterRepeat(ac);
					example.setOuterRepeatNum(outerRepeatNum);
					dao.updateExampleOuterRepeatNum(example);
				}
			}
		}
		tempDir.delete();
		System.out.println("repository "+repositoryId+" end extractor*****************************************************************************************************");
	}
	public void matchChangeAndApi(List<Apichange> apiChangeList, ChangeLine line, Map<Integer, JdkSequence> jdkCall, ChangeFile changeFile){
		if(jdkCall==null) return;
		JdkSequence sequence = jdkCall.get(line.getLineNum());
		if(sequence!=null){
			for(MethodCall s: sequence.getApiList()){
				Apichange apichange = new Apichange();
				apichange.setRepositoryId(repositoryId);
				apichange.setCommitId(changeFile.getCommitId());
				apichange.setParentCommitId(changeFile.getParentCommitId());
				apichange.setNewFileName(changeFile.getNewPath());
				apichange.setOldFileName(changeFile.getOldPath());
//				apichange.setLineNumber(line.getLineNum());
				apichange.setChangeType(line.getType());
//				apichange.setContent(line.getSequence());
//				apichange.setCompleteClassName(s.getCompleteClassName());
//				apichange.setMethodName(s.getMethodName());
//				apichange.setParameter(s.getParameter());
				apiChangeList.add(apichange);
			}
		}
	}
//	public void extractApiChange(){
//		String userDirPath = System.getProperty("user.dir");
//		String tempDirPath = userDirPath + "/" + UUID.randomUUID().toString();
//		File tempDir = new File(tempDirPath);
//		tempDir.mkdirs();
//		List<RevCommit> commits = gitReader.getCommits();
//		for(RevCommit commit : commits){
//			System.out.println("=======================================================================================================================================================");
//			List<ChangeFile> changeFiles = gitReader.getChangeFiles(commit);
//			for(ChangeFile changeFile : changeFiles){
//				if(DiffEntry.ChangeType.MODIFY.toString().equals(changeFile.getChangeType())&&changeFile.getNewPath().endsWith(".java")){
//					System.out.println("******************************************************************************************************************************************************");
//					System.out.println(changeFile.getNewPath());
//					byte[] newContent = gitReader.getFileByObjectId(true,changeFile.getNewBlobId());
//					byte[] oldContent = gitReader.getFileByObjectId(false,changeFile.getOldBlobId());
//					String randomString = UUID.randomUUID().toString();
//					File newFile = FileUtils.writeBytesToFile(newContent, tempDirPath, randomString + ".v1");
//					File oldFile = FileUtils.writeBytesToFile(oldContent, tempDirPath, randomString + ".v2");
//					List<SourceCodeChange> changes = changeExactor.extractChangesInFile(oldFile, newFile);
//					if(changes.size()>0){
//						Map<Integer, JdkSequence> jdkCall = constructData(newFile);
//						matchChangeAndApi(changes, jdkCall);
//					}
//					newFile.delete();
//					oldFile.delete();
//				}
//			}
//		}
//		tempDir.delete();
//	}
//	public void matchChangeAndApi(List<SourceCodeChange> changes, Map<Integer, JdkSequence> jdkCall) {
//		if(jdkCall==null){
//			for(SourceCodeChange change:changes){
//				System.out.println(change.getClass().getSimpleName() + ": " + change.getChangedEntity().getUniqueName()+"/ "+change.getLabel());
//			}
//		}else{
//			for(SourceCodeChange change:changes){
//				System.out.println(change.getClass().getSimpleName() + ": " + change.getChangedEntity().getUniqueName()+"/ "+change.getLabel());
//				if(change instanceof Update){
//					matchUpdate((Update) change, jdkCall);
//				}else if(change instanceof Move){
//					matchMove((Move) change, jdkCall);	
//				}else if(change instanceof Insert){
//					matchInsert((Insert) change, jdkCall);
//				}else{
//					matchDelete((Delete) change, jdkCall);
//				}
//			}
//		}
//	}
//	public void matchUpdate(Update change, Map<Integer, JdkSequence> jdkCall){
//		
//	}
//	public void matchMove(Move change,Map<Integer, JdkSequence> jdkCall){
//		
//	}
//	public void matchInsert(Insert change, Map<Integer, JdkSequence> jdkCall){
//		
//	}
//	public void matchDelete(Delete change, Map<Integer, JdkSequence> jdkCall){
//		
//	}

	public Map<Integer, JdkSequence> constructData(File file){
		Map<Integer, JdkSequence> result = new HashMap<Integer, JdkSequence>();
		if(file.length()/1048576>1) return result;
        JapaAst japaAst = new JapaAst(true);
        List<String> tempList = new ArrayList<>();
        CompilationUnit cu = null;
		try {
			cu = JavaParser.parse(file);
	        tempList = japaAst.parse(cu);
		} catch (ParseException e1) {
            System.err.println("CU Error");
			return result;
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

        //如果Import的包中带有*号，那么得到含有*号的这个import
		if(cu==null) return null;
        List<ImportDeclaration> importList = cu.getImports();
        List<String> starImportStringList = new ArrayList<>();
        try{
        if (importList != null) {
            for (int i = 0; i < importList.size(); i++) {
                if (importList.get(i).toString().contains("*")) {
                    String str = importList.get(i).toString();
                    int index = str.indexOf("import");
                    str = str.substring(index);
                    String[] strs = str.split(" ");
                    str = strs[strs.length - 1];//得到Import的包的信息
                    str = str.replace(" ", ""); //替换掉空格" "
                    str = str.replace(";", ""); //去除;
                    starImportStringList.add(str);
                }

            }
        }
        }catch(Exception e){
        	e.printStackTrace();
        	return null;
        }catch(Error e){
        	e.printStackTrace();
        }
        
        //开始分析程序
        if (cu.getTypes() != null) {
            for (TypeDeclaration type : cu.getTypes()) {
                if (type instanceof ClassOrInterfaceDeclaration) {
                    //处理field
                    List<VariableDeclarationExpr> fieldExpressionList = new ArrayList<>();
                    for (BodyDeclaration body : type.getMembers()) {
                        if (body instanceof FieldDeclaration) {
                            FieldDeclaration field = (FieldDeclaration) body;
                            for (int i = 0; i < field.getVariables().size(); i++) {
                                VariableDeclarationExpr expr = new VariableDeclarationExpr();
                                List list = new ArrayList();
                                list.add(field.getVariables().get(i));
                                expr.setType(field.getType());
                                expr.setVars(list);
                                fieldExpressionList.add(expr);
                            }
                        }
                    }
                    //处理method
                    for (BodyDeclaration body : type.getMembers()) {
                        if (body instanceof MethodDeclaration) {
                            if ((body.getEndLine() - body.getBeginLine() <= 826)) {
                                List<String> completeClassNameList = new ArrayList<>();
                                for (String str : tempList) {
                                    completeClassNameList.add(str);
                                }
                                List userClassList = new ArrayList();
                                for (String str : japaAst.getFilternames()) {
                                    userClassList.add(str);
                                }
                                UserClassProcessing userClassProcessing = new UserClassProcessing();
                                userClassProcessing.setUserClassList(userClassList);
                                userClassList.add("userDefinedClass");
                                MethodDeclaration method = (MethodDeclaration) body;
                                List<String> parameterNameList = new ArrayList<>();
                                List<String> typeMapList = new ArrayList<>();
                                List<String> completeTypeMapList = new ArrayList<>();
                                List<ExpressionStmt> parameterExpressionList = new ArrayList<>();
                                if (method.getParameters() != null) {
                                    List<Parameter> parameterList = method.getParameters();
                                    for (int i = 0; i < parameterList.size(); i++) {
                                        String contentString = "public class Test{public void test(){$}}";
                                        String parameterString = parameterList.get(i).toString() + ";";
                                        contentString = contentString.replaceAll("\\$", java.util.regex.Matcher.quoteReplacement(parameterString));
                                        InputStream in = new ByteArrayInputStream(contentString.getBytes());
                                        try {
                                            CompilationUnit compilationUnit = JavaParser.parse(in);
                                            Node node = compilationUnit.getTypes().get(0).getMembers().get(0);
                                            ExpressionStmt expression = (ExpressionStmt) node.getChildrenNodes().get(1).getChildrenNodes().get(0);
                                            parameterExpressionList.add(expression);
                                        } catch (Exception e) {
                                            continue;
                                        } catch (Error e) {
                                            continue;
                                        }
                                    }
                                }
                                /*添加类中的成员变量*/
                                SimplifiedTreeCreator creator = new SimplifiedTreeCreator("");
                                creator.setUserClassProcessing(userClassProcessing);
                                creator.setStarImportStringList(starImportStringList);
                                List<String> tempUserClassList = new ArrayList<>();
                                for (int i = 0; i < completeClassNameList.size(); i++) {
                                    try {
                                        Class clazz = Thread.currentThread().getContextClassLoader().loadClass(completeClassNameList.get(i));
                                        creator.getClass_name_map().put(clazz.getSimpleName(), completeClassNameList.get(i));
                                    } catch (Exception e) {
                                        tempUserClassList.add(completeClassNameList.get(i));
                                        userClassList.add(completeClassNameList.get(i));
                                    } catch (Error e) {
                                        //System.err.println(e.getCause());
                                        tempUserClassList.add(completeClassNameList.get(i));
                                        userClassList.add(completeClassNameList.get(i));
                                    }

                                }
                                //过滤掉反射不到的类
                                for (int i = 0; i < tempUserClassList.size(); i++) {
                                    completeClassNameList.remove(tempUserClassList.get(i));
                                }
                                tempUserClassList.removeAll(tempUserClassList);
                                //处理field
                                for (int i = 0; i < fieldExpressionList.size(); i++) {
                                    creator.convert(fieldExpressionList.get(i));
                                }
                                //处理method中的parameter
                                for (int i = 0; i < parameterExpressionList.size(); i++) {
                                    creator.convert(parameterExpressionList.get(i));
                                }
                                CodeTree codeTree = constructTreeFromAST(completeClassNameList, parameterNameList, typeMapList,
                                        completeTypeMapList, starImportStringList, method, creator, userClassProcessing, true, "");
                                if (codeTree != null && codeTree.getRoot() != null && codeTree.getTotalNumber() <= 1574) {
                                	/*display the code tree*/
                                	result.putAll(codeTree.getJdkCall());
                                    displayTree(codeTree, true, method.getName() + (method.getParameters() == null ? "[]" : method.getParameters()));
                                } else {
                                    System.err.println("So " + method.getName() + (method.getParameters() == null ? "[]" : method.getParameters()) + " (" + ") " + " can not be correctly parsed");
//                                    return null;
                                }
                            }
                        }
                    }
                }
            }
        }
		return result;
    }
	public CodeTree constructTreeFromAST(List<String> completeClassNameList, List<String> parameterNameList,
            List<String> typeMapList, List<String> completeTypeMapList,
            List<String> starImportStringList, MethodDeclaration method,
            SimplifiedTreeCreator fieldCreator, UserClassProcessing userClassProcessing,
            boolean holeFlag, String globalPath) {
		try {
		SimplifiedTreeCreator creator = new SimplifiedTreeCreator(completeClassNameList, fieldCreator, globalPath);
		creator.setHoleFlag(holeFlag);
		for (int i = 0; i < parameterNameList.size(); i++) {
			creator.addClass_variable_list(parameterNameList.get(i));
		}
		for (int i = 0; i < typeMapList.size(); i++) {
			String[] strings = typeMapList.get(i).split(" ");
			creator.addClass_variable(strings[0], strings[1]);
		}
		for (int i = 0; i < completeTypeMapList.size(); i++) {
			creator.addClass_name_map(completeTypeMapList.get(i));
		}
		creator.setStarImportStringList(starImportStringList);
		creator.setUserClassProcessing(userClassProcessing);
		creator.toCodeTree(method);
		CodeTree codeTree = new CodeTree();
		codeTree.setJdkCall(creator.getCodeTree().getJdkCall());
		codeTree.setRoot(creator.getCodeTree().getRoot());
		if (creator.getParsedFlag()) {
			return codeTree;
		} else {
			return null;
		}
		} catch (Exception e) {
			return null;
		} catch (Error e) {
			return null;
		}
	}
		
	public void displayTree(CodeTree codeTree, boolean isCompleteFlag, String title) {
		TreeView treeView = new TreeView();
		treeView.convertCodeTree(codeTree, isCompleteFlag);
		DisplayTreeView display = new DisplayTreeView(treeView.getTree(), title);
	}
}

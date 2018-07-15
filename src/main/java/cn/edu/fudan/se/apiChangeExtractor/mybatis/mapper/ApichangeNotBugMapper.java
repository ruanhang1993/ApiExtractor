package cn.edu.fudan.se.apiChangeExtractor.mybatis.mapper;

import java.util.List;

import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.APIRank;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.Api;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.Apichange;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.ChangeExample;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.InnerChangeExample;

public interface ApichangeNotBugMapper {
	public void insertApichange(Apichange apichange);
	public void insertApichangeList(List<Apichange> apichanges);
	public void updateRepeat(Apichange apichange);
	public int countInnerRepeat(Apichange apichange);
	public int countOuterRepeat(Apichange apichange);
	public void updateRepeatOldHaveNull(Apichange apichange);
	public int countInnerRepeatOldHaveNull(Apichange apichange);
	public int countOuterRepeatOldHaveNull(Apichange apichange);
	public void updateRepeatNewHaveNull(Apichange apichange);
	public int countInnerRepeatNewHaveNull(Apichange apichange);
	public int countOuterRepeatNewHaveNull(Apichange apichange);
	public int countAll();
	public List<Apichange> selectAll();
	public void updateInnerRepeat(Apichange apichange);
	public void updateOuterRepeat(Apichange apichange);
	public void updateInnerRepeatOldHaveNull(Apichange apichange);
	public void updateOuterRepeatOldHaveNull(Apichange apichange);
	public void updateInnerRepeatNewHaveNull(Apichange apichange);
	public void updateOuterRepeatNewHaveNull(Apichange apichange);
	public List<ChangeExample> selectExample(ChangeExample example);
	public List<ChangeExample> selectExampleOldHaveNull(ChangeExample example);
	public List<ChangeExample> selectExampleNewHaveNull(ChangeExample example);
	public void insertExample(ChangeExample example);
	public void insertExampleList(List<ChangeExample> examples);
	public void updateExampleId(Apichange apichang);
	public void updateExampleIdOldHaveNull(Apichange apichang);
	public void updateExampleIdNewHaveNull(Apichange apichang);
	public int countExample(ChangeExample example);
	public int countExampleOldHaveNull(ChangeExample example);
	public int countExampleNewHaveNull(ChangeExample example);
	public void updateExampleOuterRepeatNum(ChangeExample example);
	public void insertInnerExample(InnerChangeExample innerExample);
	public void insertInnerExampleList(List<InnerChangeExample> innerExamples);
	public List<InnerChangeExample> selectInnerExample(InnerChangeExample innerExample);
	public void updateInnerExample(InnerChangeExample innerExample);
	public List<Api> selectAPIList();
	public List<Apichange> selectAPIListWithExampleId(Api apichange);
	public void insertApi(APIRank apirank);
	public List<APIRank> selectAPIs();
	public List<ChangeExample> selectExamplesByApi(Api api);

}

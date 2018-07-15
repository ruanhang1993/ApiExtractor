package cn.edu.fudan.se.apiChangeExtractor.mybatis.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.APIRank;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.Api;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.Apichange;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.ChangeExample;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.InnerChangeExample;
import cn.edu.fudan.se.apiChangeExtractor.mybatis.mapper.ApichangeNotBugMapper;

public class ApichangeNotBugDao {
	private SqlSessionFactory sqlSessionFactory = MybatisFactory.getSqlSessionFactoryTemp();
	private static class SingletonHolder {  
		private static ApichangeNotBugDao singleton;
		static {
			singleton = new ApichangeNotBugDao();
		}
	}
	public static ApichangeNotBugDao getInstance(){
		return SingletonHolder.singleton;
	}
	
	public void insertOneApichange(Apichange apichange){
		SqlSession sqlSession = sqlSessionFactory.openSession();
		
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).insertApichange(apichange);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}
	
	public void insertApichangeList(List<Apichange> apichanges){
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).insertApichangeList(apichanges);
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}
	
	public void updateRepeat(Apichange apichange){
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).updateRepeat(apichange);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}
	
	public int countInnerRepeat(Apichange apichange){
		int res = 0;
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			res = sqlSession.getMapper(ApichangeNotBugMapper.class).countInnerRepeat(apichange);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
		return res;
	}
	
	public int countOuterRepeat(Apichange apichange){
		int res = 0;
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			res = sqlSession.getMapper(ApichangeNotBugMapper.class).countOuterRepeat(apichange);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
		return res;
	}
	
	public void updateRepeatOldHaveNull(Apichange apichange){
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).updateRepeatOldHaveNull(apichange);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}
	
	public int countInnerRepeatOldHaveNull(Apichange apichange){
		int res = 0;
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			res = sqlSession.getMapper(ApichangeNotBugMapper.class).countInnerRepeatOldHaveNull(apichange);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
		return res;
	}
	
	public int countOuterRepeatOldHaveNull(Apichange apichange){
		int res = 0;
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			res = sqlSession.getMapper(ApichangeNotBugMapper.class).countOuterRepeatOldHaveNull(apichange);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
		return res;
	}
	public void updateRepeatNewHaveNull(Apichange apichange){
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).updateRepeatNewHaveNull(apichange);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}
	
	public int countInnerRepeatNewHaveNull(Apichange apichange){
		int res = 0;
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			res = sqlSession.getMapper(ApichangeNotBugMapper.class).countInnerRepeatNewHaveNull(apichange);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
		return res;
	}
	
	public int countOuterRepeatNewHaveNull(Apichange apichange){
		int res = 0;
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			res = sqlSession.getMapper(ApichangeNotBugMapper.class).countOuterRepeatNewHaveNull(apichange);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
		return res;
	}
	
	public int countAll(){
		int res = 0;
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			res = sqlSession.getMapper(ApichangeNotBugMapper.class).countAll();;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
		return res;
	}
	
	public List<Apichange> selectAll(){
		List<Apichange> res = null;
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			res = sqlSession.getMapper(ApichangeNotBugMapper.class).selectAll();;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
		return res;
	}
	
	public List<ChangeExample> selectExample(ChangeExample example){
		List<ChangeExample> res = null;
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			res = sqlSession.getMapper(ApichangeNotBugMapper.class).selectExample(example);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
		return res;
	}
	public List<ChangeExample> selectExampleOldHaveNull(ChangeExample example){
		List<ChangeExample> res = null;
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			res = sqlSession.getMapper(ApichangeNotBugMapper.class).selectExampleOldHaveNull(example);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
		return res;
	}
	public List<ChangeExample> selectExampleNewHaveNull(ChangeExample example){
		List<ChangeExample> res = null;
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			res = sqlSession.getMapper(ApichangeNotBugMapper.class).selectExampleNewHaveNull(example);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
		return res;
	}
	public void insertExample(ChangeExample example){
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).insertExample(example);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}
	public void insertExampleList(List<ChangeExample> examples){
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).insertExampleList(examples);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}
	public void updateExampleId(Apichange apichange){
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).updateExampleId(apichange);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}
	public void updateExampleOuterRepeatNum(ChangeExample example){
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).updateExampleOuterRepeatNum(example);
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}
	public void updateExampleIdOldHaveNull(Apichange apichange){
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).updateExampleIdOldHaveNull(apichange);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}
	public void updateExampleIdNewHaveNull(Apichange apichange){
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).updateExampleIdNewHaveNull(apichange);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}

	public void updateInnerRepeat(Apichange apichange) {
		// TODO Auto-generated method stub
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).updateInnerRepeat(apichange);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}

	public void updateOuterRepeat(Apichange apichange) {
		// TODO Auto-generated method stub
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).updateOuterRepeat(apichange);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}

	public void updateInnerRepeatOldHaveNull(Apichange apichange) {
		// TODO Auto-generated method stub
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).updateInnerRepeatOldHaveNull(apichange);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}

	public void updateOuterRepeatOldHaveNull(Apichange apichange) {
		// TODO Auto-generated method stub
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).updateOuterRepeatOldHaveNull(apichange);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}

	public void updateInnerRepeatNewHaveNull(Apichange apichange) {
		// TODO Auto-generated method stub
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).updateInnerRepeatNewHaveNull(apichange);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}

	public void updateOuterRepeatNewHaveNull(Apichange apichange) {
		// TODO Auto-generated method stub
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).updateOuterRepeatNewHaveNull(apichange);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}
	public int countExample(ChangeExample example){
		int res = 0;
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			res = sqlSession.getMapper(ApichangeNotBugMapper.class).countExample(example);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
		return res;
	}
	public int countExampleOldHaveNull(ChangeExample example){
		int res = 0;
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			res = sqlSession.getMapper(ApichangeNotBugMapper.class).countExampleOldHaveNull(example);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
		return res;
	}
	public int countExampleNewHaveNull(ChangeExample example){
		int res = 0;
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			res = sqlSession.getMapper(ApichangeNotBugMapper.class).countExampleNewHaveNull(example);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
		return res;
	}
	public void insertInnerExample(InnerChangeExample innerExample){
		SqlSession sqlSession = sqlSessionFactory.openSession();
		
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).insertInnerExample(innerExample);
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}
	public void insertInnerExampleList(List<InnerChangeExample> innerExamples){
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).insertInnerExampleList(innerExamples);
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}
	public List<InnerChangeExample> selectInnerExample(InnerChangeExample innerExample){
		List<InnerChangeExample> res = null;
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			res = sqlSession.getMapper(ApichangeNotBugMapper.class).selectInnerExample(innerExample);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
		return res;
	}
	public void updateInnerExample(InnerChangeExample innerExample){
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).updateInnerExample(innerExample);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}
	
	public List<Api> selectAPIList(){
		List<Api> res = null;
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			res = sqlSession.getMapper(ApichangeNotBugMapper.class).selectAPIList();;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
		return res;
	}
	
	public List<Apichange> selectAPIListWithExampleId(Api api){
		List<Apichange> res = null;
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			res = sqlSession.getMapper(ApichangeNotBugMapper.class).selectAPIListWithExampleId(api);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
		return res;
	}

	public void insertApi(APIRank apirank){
		SqlSession sqlSession = sqlSessionFactory.openSession();
		
		try{
			sqlSession.getMapper(ApichangeNotBugMapper.class).insertApi(apirank);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
	}
	public List<APIRank> selectAPIs(){
		List<APIRank> res = null;
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			res = sqlSession.getMapper(ApichangeNotBugMapper.class).selectAPIs();;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
		return res;
	}
	public List<ChangeExample> selectExamplesByApi(Api api){
		List<ChangeExample> res = null;
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try{
			res = sqlSession.getMapper(ApichangeNotBugMapper.class).selectExamplesByApi(api);;
			sqlSession.commit();
		}finally {
		    sqlSession.close();
		}
		return res;
	}

}

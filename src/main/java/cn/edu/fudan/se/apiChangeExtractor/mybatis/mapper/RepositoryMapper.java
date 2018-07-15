package cn.edu.fudan.se.apiChangeExtractor.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.edu.fudan.se.apiChangeExtractor.mybatis.bean.Repository;

public interface RepositoryMapper {
    public List<Repository> selectAllRepository();
    public List<Repository> selectInScope(@Param("start")int start, @Param("end")int end);
    public List<Repository> selectByStar(@Param("stars")int stars);
}
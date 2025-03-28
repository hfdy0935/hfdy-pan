package com.hfdy.hfdypan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hfdy.hfdypan.domain.entity.File;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author hf-dy
 * @date 2025/1/17 10:53
 */
@Mapper
public interface FileMapper extends BaseMapper<File> {

    /**
     * 根据id彻底删除文件
     *
     * @param id
     * @return
     */
    @Delete("delete from file where id=#{id}")
    Integer deleteCompletelyById(@Param("id") String id);


    /**
     * 获取用户的已删除文件
     *
     * @param userId
     * @return
     */
    @Select("select * from file where user_id=#{userId} and is_deleted=1")
    List<File> getDeletedFiles(String userId);

    /**
     * 根据ids获取文件，包括删除的
     *
     * @param ids
     * @return
     */
    @Select("<script>" +
            "select * from file where id in " +
            "<foreach item='id' collection='list' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<File> getAllFilesByIds(@Param("list") List<String> ids);

    /**
     * 批量恢复文件
     *
     * @param files
     */
    @Update("<script>" +
            "<foreach collection='files' item='file' separator=';' open='' close=''>" +
            "UPDATE file SET is_deleted = 0, update_time = NOW(), delete_time = null, pid = #{file.pid}, level = #{file.level} " +
            "WHERE id = #{file.id}" +
            "</foreach>" +
            "</script>")
    void recoverFiles(List<File> files);
}

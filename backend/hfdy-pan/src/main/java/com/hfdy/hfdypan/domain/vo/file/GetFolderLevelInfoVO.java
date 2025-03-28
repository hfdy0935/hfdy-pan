package com.hfdy.hfdypan.domain.vo.file;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hf-dy
 * @date 2025/3/10 00:04
 */
@Data
public class GetFolderLevelInfoVO {
    private String id;
    private String name;
    private List<GetFolderLevelInfoVO> children;
    private Integer status;
    private String level;
    private String category;
    private String mediaType;
    private String lyricPath;

    public static GetFolderLevelInfoVO newWithChildren() {
        GetFolderLevelInfoVO vo = new GetFolderLevelInfoVO();
        List<GetFolderLevelInfoVO> children = new ArrayList<>();
        vo.setChildren(children);
        return vo;
    }
}

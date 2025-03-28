package com.hfdy.hfdypan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hfdy.hfdypan.domain.dto.file.SaveShareToMyPanDTO;
import com.hfdy.hfdypan.domain.dto.file.ShareFileDTO;
import com.hfdy.hfdypan.domain.entity.File;
import com.hfdy.hfdypan.domain.entity.Share;
import com.hfdy.hfdypan.domain.vo.file.GetShareFileVO;
import com.hfdy.hfdypan.domain.vo.file.ShareFileVO;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * @author hf-dy
 * @date 2025/3/7 17:05
 */
public interface TransferFileService extends IService<Share> {

    /**
     * 分享文件
     *
     * @param dto
     * @param fileList
     */
    ShareFileVO shareFiles(ShareFileDTO dto, List<File> fileList);

    /**
     * 获取分享的文件
     *
     * @param share
     * @param pid
     * @return
     */
    GetShareFileVO getSharedFile(Share share, String pid);


    /**
     * 以zip格式下载
     *
     * @param fileList
     * @param speed    限速
     * @return
     */
    void downloadAsZip(List<File> fileList, HttpServletResponse response, long speed) throws Exception;

    /**
     * 把分享文件保存到我的网盘
     *
     * @param dto
     * @param to  文件夹或null
     */
    void saveShareToMyPan(SaveShareToMyPanDTO dto, File to);


}

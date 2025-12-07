package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.entity.system.SysSfc;
import org.apache.ibatis.annotations.Param;

import java.util.List;
/**
 * @author CH
 * @since 2024/10/24
 */
public interface SysSfcMapper extends BaseMapper<SysSfc> {
    /**
     * 鏍规嵁鏉′欢分页查询SysSfc琛ㄤ腑鐨勬暟鎹?
     * 姝ゆ柟娉曞厑璁搁€氳繃浼犲叆一个猄ysSfc瀵硅薄浣滀负鏉′欢参数锛岀粨鍚堝垎椤典俊鎭紝鏉ユ煡璇㈢鍚堢壒瀹氭潯浠剁殑SysSfc记录
     *
     * @param page 分页瀵硅薄锛屽寘鍚垎椤靛弬鏁板当前椤电爜鍜屾瘡椤靛ぇ灏?
     * @param sysSfc 鍖呭惈查询鏉′欢鐨凷ysSfc瀵硅薄
     * @param userId 用户ID锛岀敤浜庨檺瀹氭煡璇㈡潯浠舵垨记录查询用户鐨勬爣璇?
     * @return 返回一个垎椤靛璞★紝鍖呭惈查询结果鍜屽垎椤典俊鎭?
     */
    IPage<SysSfc> selectPageForSfc(@Param("page") Page<SysSfc> page, @Param("condition") SysSfc sysSfc, @Param("userId") String userId);

    /**
     * 查询当前用户涓嬬殑SysSfc数据
     * 姝ゆ柟娉曠敤浜庢牴鎹敤鎴稩D鍜孲ysSfc绫诲埆查询灞炰簬当前用户鐨勬墍鏈塖ysSfc记录
     * 涓昏鐢ㄤ簬获取用户鐩稿叧鐨凷ysSfc信息锛屼互渚胯繘琛岀敤鎴风骇鍒殑数据处理鎴栧睍绀"
     *
     * @param sysSfcCategory SysSfc绫诲埆锛岀敤浜庨檺瀹氭煡璇㈢殑SysSfc类型
     * @param userId 用户ID锛岀敤浜庨檺瀹氭煡璇㈠睘浜庡摢涓敤鎴风殑数据
     * @return 返回一个垪琛紝鍖呭惈所有夌鍚堟煡璇㈡潯浠剁殑SysSfc瀵硅薄
     */
    List<SysSfc> mine(@Param("sysSfcCategory") String sysSfcCategory, @Param("userId") String userId);
}

package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.system.entity.SysSfc;
import org.apache.ibatis.annotations.Param;

import java.util.List;
/**
 * @author CH
 * @since 2024/10/24
 */
public interface SysSfcMapper extends BaseMapper<SysSfc> {
    /**
     * 鏍规嵁鏉′欢鍒嗛〉鏌ヨSysSfc琛ㄤ腑鐨勬暟鎹?
     * 姝ゆ柟娉曞厑璁搁€氳繃浼犲叆涓€涓猄ysSfc瀵硅薄浣滀负鏉′欢鍙傛暟锛岀粨鍚堝垎椤典俊鎭紝鏉ユ煡璇㈢鍚堢壒瀹氭潯浠剁殑SysSfc璁板綍
     *
     * @param page 鍒嗛〉瀵硅薄锛屽寘鍚垎椤靛弬鏁板褰撳墠椤电爜鍜屾瘡椤靛ぇ灏?
     * @param sysSfc 鍖呭惈鏌ヨ鏉′欢鐨凷ysSfc瀵硅薄
     * @param userId 鐢ㄦ埛ID锛岀敤浜庨檺瀹氭煡璇㈡潯浠舵垨璁板綍鏌ヨ鐢ㄦ埛鐨勬爣璇?
     * @return 杩斿洖涓€涓垎椤靛璞★紝鍖呭惈鏌ヨ缁撴灉鍜屽垎椤典俊鎭?
     */
    IPage<SysSfc> selectPageForSfc(@Param("page") Page<SysSfc> page, @Param("condition") SysSfc sysSfc, @Param("userId") String userId);

    /**
     * 鏌ヨ褰撳墠鐢ㄦ埛涓嬬殑SysSfc鏁版嵁
     * 姝ゆ柟娉曠敤浜庢牴鎹敤鎴稩D鍜孲ysSfc绫诲埆鏌ヨ灞炰簬褰撳墠鐢ㄦ埛鐨勬墍鏈塖ysSfc璁板綍
     * 涓昏鐢ㄤ簬鑾峰彇鐢ㄦ埛鐩稿叧鐨凷ysSfc淇℃伅锛屼互渚胯繘琛岀敤鎴风骇鍒殑鏁版嵁澶勭悊鎴栧睍绀"
     *
     * @param sysSfcCategory SysSfc绫诲埆锛岀敤浜庨檺瀹氭煡璇㈢殑SysSfc绫诲瀷
     * @param userId 鐢ㄦ埛ID锛岀敤浜庨檺瀹氭煡璇㈠睘浜庡摢涓敤鎴风殑鏁版嵁
     * @return 杩斿洖涓€涓垪琛紝鍖呭惈鎵€鏈夌鍚堟煡璇㈡潯浠剁殑SysSfc瀵硅薄
     */
    List<SysSfc> mine(@Param("sysSfcCategory") String sysSfcCategory, @Param("userId") String userId);
}

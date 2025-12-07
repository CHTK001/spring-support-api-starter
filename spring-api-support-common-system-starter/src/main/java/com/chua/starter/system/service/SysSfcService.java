package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.system.SysSfc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author CH
 * @since 2024/10/24
 */
public interface SysSfcService extends IService<SysSfc> {


    /**
     * 鍒嗛〉鍒楄〃
     *
     * @param query   鏌ヨ鏉′欢
     * @param sysSfc 鏌ヨ鏉′欢
     * @param roles   瑙掕壊
     * @param userId  鐢ㄦ埛id
     * @return 鍒嗛〉鍒楄〃
     */
    IPage<SysSfc> pageForUserId(Query<SysSfc> query, SysSfc sysSfc, Set<String> roles, String userId);


    /**
     * 涓婁紶缁勪欢
     *
     * @param file    鏂囦欢
     * @param sysSfc 缁勪欢淇℃伅
     * @return 涓婁紶缁撴灉
     */
    ReturnResult<Boolean> uploadSfc(MultipartFile file, SysSfc sysSfc);

    /**
     * 淇濆瓨缁勪欢
     *
     * @param sysSfc 缁勪欢淇℃伅
     * @param roles  瑙掕壊
     * @return 淇濆瓨缁撴灉
     */
    ReturnResult<SysSfc> save(SysSfc sysSfc, Set<String> roles);

    /**
     * 鏇存柊缁勪欢
     * @param sysSfc 缁勪欢淇℃伅
     * @param roles 瑙掕壊
     * @return 鏇存柊缁撴灉
     */
    ReturnResult<Boolean> updateById(SysSfc sysSfc, Set<String> roles);

    /**
     * 鍒犻櫎缁勪欢
     * @param sfcId 缁勪欢id
     * @param roles 瑙掕壊
     * @return 鍒犻櫎缁撴灉
     */
    ReturnResult<Boolean> delete(Long sfcId, Set<String> roles);

    /**
     * 瀹夎缁勪欢
     * @param sysSfc 缁勪欢淇℃伅
     * @param userId 鐢ㄦ埛id
     * @return 瀹夎缁撴灉
     */
    ReturnResult<Boolean> install(SysSfc sysSfc, Integer userId);

    /**
     * 鍗歌浇缁勪欢
     * @param sysSfc 缁勪欢淇℃伅
     * @param userId 鐢ㄦ埛id
     * @return 鍗歌浇缁撴灉
     */
    ReturnResult<Boolean> uninstall(SysSfc sysSfc, Integer userId);

    /**
     * 鑾峰彇缁勪欢
     * @param sysSfcId 缁勪欢id
     * @param roles 瑙掕壊
     * @param userId 鐢ㄦ埛id
     * @return 缁勪欢淇℃伅
     */
    ReturnResult<String> get(Long sysSfcId, Set<String> roles, String userId);

    /**
     * 鎴戠殑缁勪欢
     * @param sysSfcCategory 缁勪欢鍒嗙被
     * @param userId 鐢ㄦ埛id
     * @return 缁勪欢鍒楄〃
     */
    ReturnResult<List<SysSfc>> mine(String sysSfcCategory, String userId);
}

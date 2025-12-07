package com.chua.starter.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.system.SysDept;
import com.chua.starter.system.pojo.dept.SysDeptVO;

import java.util.List;
import java.util.Set;

/**
 * 绯荤粺閮ㄩ棬鏈嶅姟鎺ュ彛
 * 璇ユ帴鍙ｅ畾涔変簡涓庣郴缁熼儴闂ㄧ浉鍏崇殑涓氬姟鎿嶄綔锛屽寘鎷煡璇€佸鍒犳敼绛?
 * @since 2024/8/7
 * @author CH
 */
public interface SysDeptService extends IService<SysDept>{


    /**
     * 鏌ヨ閮ㄩ棬鍒楄〃
     * 鏍规嵁閮ㄩ棬鏌ヨ鏉′欢鍜岃鑹查泦鍚堬紝杩斿洖绗﹀悎鏉′欢鐨勯儴闂ㄥ垪琛?
     * @param sysDept 閮ㄩ棬鏌ヨ鏉′欢
     * @param roles 瑙掕壊闆嗗悎锛岀敤浜庢潈闄愭帶鍒舵垨绛涢€?
     * @return 閮ㄩ棬鍒楄〃锛屽寘鍚瘡涓儴闂ㄧ殑璇︾粏淇℃伅
     */
    List<SysDeptVO> list(SysDept sysDept, Set<String> roles);

    /**
     * 鍒犻櫎閮ㄩ棬
     * 鏍规嵁閮ㄩ棬ID杩涜閮ㄩ棬鍒犻櫎鎿嶄綔锛岃繑鍥炲垹闄ょ粨鏋?
     * @param deptId 閮ㄩ棬ID锛屽敮涓€鏍囪瘑瑕佸垹闄ょ殑閮ㄩ棬
     * @return 鍒犻櫎缁撴灉锛屽寘鍚垚鍔熶笌鍚︾殑鏍囧織鍜屾彁绀轰俊鎭?
     */
    ReturnResult<Boolean> delete(Long deptId);

    /**
     * 鏇存柊閮ㄩ棬淇℃伅
     * 鏍规嵁瑙掕壊闆嗗悎鍜岄儴闂ㄤ俊鎭洿鏂伴儴闂紝杩斿洖鏇存柊缁撴灉
     * @param roles 瑙掕壊闆嗗悎锛岀敤浜庢潈闄愭帶鍒舵垨绛涢€?
     * @param sysDept 寰呮洿鏂扮殑閮ㄩ棬淇℃伅
     * @return 鏇存柊缁撴灉锛屽寘鍚垚鍔熶笌鍚︾殑鏍囧織鍜屾彁绀轰俊鎭?
     */
    ReturnResult<Boolean> update(Set<String> roles, SysDept sysDept);

    /**
     * 鏂板閮ㄩ棬
     * 鏍规嵁閮ㄩ棬淇℃伅鍜岃鑹查泦鍚堬紝鏂板涓€涓儴闂紝杩斿洖鏂板缁撴灉
     * @param sysDept 寰呮柊澧炵殑閮ㄩ棬淇℃伅
     * @param roles 瑙掕壊闆嗗悎锛岀敤浜庢潈闄愭帶鍒舵垨绛涢€?
     * @return 鏂板缁撴灉锛屽寘鍚垚鍔熶笌鍚︾殑鏍囧織鍜屾彁绀轰俊鎭?
     */
    ReturnResult<SysDept> save(SysDept sysDept, Set<String> roles);
}


package com.chua.starter.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.system.SysPost;
import com.chua.starter.entity.system.SysUser;

import java.util.List;

/**
* 岗位服务接口
*
* @since 4.0.0.43
* @author CH
 */
public interface SysPostService extends IService<SysPost> {

    /**
    * 查询全部岗位列表(启用的岗位, 按排序升序)
    *
    * @param sysPost 查询条件
    * @return 岗位列表
     */
    List<SysPost> list(SysPost sysPost);

    /**
    * 分页查询岗位
    *
    * @param query   分页参数
    * @param sysPost 查询条件
    * @return 岗位分页
     */
    IPage<SysPost> page(Query<SysPost> query, SysPost sysPost);

    /**
    * 新增岗位
    *
    * @param sysPost 岗位
    * @return 新增后的岗位
     */
    ReturnResult<SysPost> savePost(SysPost sysPost);

    /**
    * 更新岗位
    *
    * @param sysPost 岗位
    * @return 是否成功
     */
    ReturnResult<Boolean> updatePost(SysPost sysPost);

    /**
    * 删除岗位(存在用户关联时拒绝)
    *
    * @param postId 岗位标识
    * @return 是否成功
     */
    ReturnResult<Boolean> deletePost(Integer postId);

    /**
    * 查询岗位下的用户列表
    *
    * @param postId 岗位标识
    * @return 用户列表
     */
    List<SysUser> getUsersByPostId(Integer postId);
}

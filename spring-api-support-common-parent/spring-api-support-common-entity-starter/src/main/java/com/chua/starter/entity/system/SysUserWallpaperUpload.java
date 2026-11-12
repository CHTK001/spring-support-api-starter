package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户上传壁纸记录表
 *
 * <p>只记「谁传过哪张图」，文件本体在文件存储里。删记录不删文件，
 * 避免误删被其它账号或页面引用的同一份对象。</p>
 *
 * @author CH
 * @since 4.0.0
 */
@Schema(description = "用户上传壁纸记录表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_wallpaper_upload")
public class SysUserWallpaperUpload extends SysBase {

    @TableId(value = "sys_user_wallpaper_upload_id", type = IdType.AUTO)
    @Schema(description = "上传记录ID")
    private Integer sysUserWallpaperUploadId;

    @TableField(value = "sys_user_id")
    @Schema(description = "用户ID")
    private Integer sysUserId;

    @TableField(value = "url")
    @Schema(description = "图片地址")
    private String url;

    @TableField(value = "name")
    @Schema(description = "展示名")
    private String name;
}

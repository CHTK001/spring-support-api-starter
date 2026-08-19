package com.chua.starter.protocol.test;

import com.chua.protocol.support.network.protocol.ServerSetting;
import com.chua.protocol.support.network.protocol.filter.FileStorageServletFilter;
import com.chua.protocol.support.network.protocol.storage.FileStorageFactory;
import com.chua.protocol.support.network.protocol.server.ProtocolServer;
import com.chua.storage.support.storage.oss.FileStorage;
import com.chua.storage.support.storage.oss.setting.BucketSetting;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * 启动 HTTP 文件存储 - 基于 {@code ProtocolServer.create("http", ...)} + 内置 {@code FileStorageServletFilter}
 * <p>
 * 验证项目自带的 {@code FileStorageServletFilter} SPI 实现与 {@code FileSystemFileStorage} 配合使用。
 * </p>
 *
 * @author CH
 */
public class StartHttpProtocolFileServer {

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        String host = args.length > 1 ? args[1] : "0.0.0.0";
        String baseDir = args.length > 2 ? args[2] : new File(".").getAbsoluteFile().getParent();

        // 确保目录存在
        new File(baseDir).mkdirs();

        // 1. 创建 HTTP 协议服务器
        ServerSetting setting = ServerSetting.builder()
                .port(port)
                .host(host)
                .build();
        setting.setAddDefaultMapping(false);
        setting.setAllowOrigin(true);

        ProtocolServer server = ProtocolServer.create("http", setting);

        // 2. 配置内置 FileStorageServletFilter
        //    使用本地文件系统作为存储后端
        FileStorageFactory.FileStorageSetting fsSetting = FileStorageFactory.FileStorageSetting.builder()
                .openDownload(true)
                .openPreview(true)
                .openWebjars(false)
                .openRemoteFile(false)
                .build();

        FileStorageServletFilter filter = new FileStorageServletFilter(fsSetting);

        // 3. 注册本地文件系统存储
        //    FileSystemFileStorage.rootPath = endpoint + "/" + bucket
        BucketSetting bucketSetting = BucketSetting.builder()
                .bucket("")
                .endpoint(baseDir)
                .accessKeyId("local")
                .accessKeySecret("local")
                .build();

        FileStorage fileStorage = FileStorage.createStorage("fileSystem", bucketSetting);
        filter.addFileStorage("/", fileStorage);

        // 4. 注册过滤器到服务器
        server.addFilterFirst(filter);

        server.start();
        server.awaitReady(5, TimeUnit.SECONDS);

        System.out.println("========================================");
        System.out.println("HTTP 文件存储已启动 (FileStorageServletFilter)");
        System.out.println("地址: http://127.0.0.1:" + port);
        System.out.println("根目录: " + new File(baseDir).getAbsolutePath());
        System.out.println("引擎: smart-http + FileStorageServletFilter");
        System.out.println("========================================");
        System.out.println("浏览器打开 http://127.0.0.1:" + port + " 即可浏览文件");
        System.out.println("按 Ctrl+C 停止服务器");
        System.out.println("========================================");

        // 保持运行
        synchronized (StartHttpProtocolFileServer.class) {
            StartHttpProtocolFileServer.class.wait();
        }
    }
}

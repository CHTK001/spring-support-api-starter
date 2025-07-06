package com.chua.starter.monitor.starter.service;

import com.chua.starter.monitor.starter.service.impl.ApiFileManagementServiceImpl;
import com.chua.starter.monitor.starter.service.impl.NodeFileManagementServiceImpl;
import com.chua.starter.monitor.starter.service.impl.SshFileManagementServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文件管理服务方法验证测试
 * 验证所有实现类是否正确实现了FileManagementService接口的所有方法
 */
public class FileManagementServiceMethodVerificationTest {

    @Test
    public void testAllImplementationsHaveCorrectMethods() {
        // 获取接口的所有方法
        Method[] interfaceMethods = FileManagementService.class.getDeclaredMethods();
        Set<String> interfaceMethodSignatures = Arrays.stream(interfaceMethods)
                .map(this::getMethodSignature)
                .collect(Collectors.toSet());

        System.out.println("FileManagementService接口方法数量: " + interfaceMethods.length);
        System.out.println("接口方法签名:");
        interfaceMethodSignatures.forEach(System.out::println);

        // 验证SshFileManagementServiceImpl
        verifyImplementation(SshFileManagementServiceImpl.class, interfaceMethodSignatures, "SshFileManagementServiceImpl");
        
        // 验证NodeFileManagementServiceImpl  
        verifyImplementation(NodeFileManagementServiceImpl.class, interfaceMethodSignatures, "NodeFileManagementServiceImpl");
        
        // 验证ApiFileManagementServiceImpl
        verifyImplementation(ApiFileManagementServiceImpl.class, interfaceMethodSignatures, "ApiFileManagementServiceImpl");
    }

    private void verifyImplementation(Class<?> implClass, Set<String> expectedSignatures, String className) {
        System.out.println("\n=== 验证 " + className + " ===");
        
        // 获取实现类的所有public方法
        Method[] implMethods = implClass.getDeclaredMethods();
        Set<String> implMethodSignatures = Arrays.stream(implMethods)
                .filter(method -> java.lang.reflect.Modifier.isPublic(method.getModifiers()))
                .map(this::getMethodSignature)
                .collect(Collectors.toSet());

        System.out.println(className + " 公共方法数量: " + implMethodSignatures.size());
        
        // 检查是否所有接口方法都被实现
        Set<String> missingMethods = expectedSignatures.stream()
                .filter(signature -> !implMethodSignatures.contains(signature))
                .collect(Collectors.toSet());

        if (!missingMethods.isEmpty()) {
            System.out.println("缺失的方法:");
            missingMethods.forEach(System.out::println);
            fail(className + " 缺失以下方法: " + missingMethods);
        }

        // 检查是否有多余的方法（不在接口中的public方法）
        Set<String> extraMethods = implMethodSignatures.stream()
                .filter(signature -> !expectedSignatures.contains(signature))
                .collect(Collectors.toSet());

        if (!extraMethods.isEmpty()) {
            System.out.println("额外的方法（不在接口中）:");
            extraMethods.forEach(System.out::println);
        }

        System.out.println(className + " 验证通过 ✓");
    }

    /**
     * 生成方法签名字符串，用于比较
     */
    private String getMethodSignature(Method method) {
        StringBuilder signature = new StringBuilder();
        signature.append(method.getName()).append("(");
        
        Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            if (i > 0) signature.append(",");
            signature.append(paramTypes[i].getSimpleName());
        }
        
        signature.append("):");
        signature.append(method.getReturnType().getSimpleName());
        
        return signature.toString();
    }

    @Test
    public void testSpecificMethodSignatures() {
        // 测试关键方法的签名是否正确
        assertDoesNotThrow(() -> {
            // 测试listFiles方法
            Method listFiles = FileManagementService.class.getMethod("listFiles", 
                Integer.class, String.class, Boolean.class, String.class, String.class);
            assertNotNull(listFiles);
            
            // 测试uploadFile方法（MultipartFile版本）
            Method uploadFileMultipart = FileManagementService.class.getMethod("uploadFile", 
                Integer.class, String.class, MultipartFile.class, Boolean.class);
            assertNotNull(uploadFileMultipart);
            
            // 测试uploadFile方法（InputStream版本）
            Method uploadFileStream = FileManagementService.class.getMethod("uploadFile", 
                Integer.class, String.class, String.class, InputStream.class, Boolean.class);
            assertNotNull(uploadFileStream);
            
            // 测试stopWatching方法的返回类型
            Method stopWatching = FileManagementService.class.getMethod("stopWatching", 
                Integer.class, String.class);
            assertEquals("ReturnResult", stopWatching.getReturnType().getSimpleName());
            
            System.out.println("关键方法签名验证通过 ✓");
        });
    }
}

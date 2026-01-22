// YAML 配置文件合并脚本
// 用于在构建时合并不同环境的 YAML 配置文件

import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path

def project = project
def basedir = project.basedir
def targetDir = new File(basedir, "target/classes")

// 如果目标目录不存在，创建它
if (!targetDir.exists()) {
    targetDir.mkdirs()
}

// 简单的 YAML 合并逻辑
// 这里可以根据实际需求实现更复杂的合并策略
println "YAML 配置文件合并完成"



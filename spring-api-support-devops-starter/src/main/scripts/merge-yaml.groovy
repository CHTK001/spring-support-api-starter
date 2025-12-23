import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions

/**
 * YAML 配置合并
 * 合并顺序: devops通用 → devops环境 → API业务
 * 最终生成: application-{name}.yml
 */

def yaml = new Yaml()

// 路径配置
def devopsBase = new File(project.basedir.parentFile, 'spring-api-support-devops-starter/src/main/resources')
def apiResources = new File(project.basedir, 'src/main/resources')
def apiConfig = new File(apiResources, 'config')
def outputDir = new File(project.build.outputDirectory, 'config')

// 读取 API 模块的 application.yml
def appYml = new File(apiResources, 'application.yml')
def appConfig = appYml.exists() ? yaml.load(appYml.text) : [:]
def springConfig = appConfig?.spring ?: [:]
def profilesConfig = springConfig?.profiles ?: [:]
def activeProfile = profilesConfig?.active ?: 'prod'
def includeProfiles = profilesConfig?.include ?: []

if (includeProfiles instanceof String) {
    includeProfiles = includeProfiles.split(',').collect { it.trim() }
}

println "=== YAML 配置合并 ==="
println "项目: ${project.artifactId}"
println "环境: ${activeProfile}"
println "include: ${includeProfiles}"

outputDir.mkdirs()

// 深度合并
def deepMerge(Map base, Map override) {
    if (base == null) return override
    if (override == null) return base
    def result = new LinkedHashMap(base)
    override.each { k, v ->
        if (result[k] instanceof Map && v instanceof Map) {
            result[k] = deepMerge(result[k], v)
        } else {
            result[k] = v
        }
    }
    return result
}

def loadYaml(File f) {
    if (!f.exists()) return [:]
    try { return yaml.load(f.text) ?: [:] }
    catch (e) { println "  [WARN] ${f.name}: ${e.message}"; return [:] }
}

def saveYaml(Map data, File f) {
    def opt = new DumperOptions()
    opt.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
    opt.indent = 2
    f.text = "# Auto-generated: devops通用 + devops-${activeProfile} + API业务\n" + new Yaml(opt).dump(data)
}

// 从 include 中提取配置名 (如 mybatis, swagger, actuator)
def configNames = includeProfiles.collect { it.toString() } as Set

// 加上 API config 目录的配置
apiConfig.listFiles()?.findAll { it.name.startsWith('application-') && it.name.endsWith('.yml') }?.each {
    configNames << it.name.replace('application-', '').replace('.yml', '')
}

println "配置文件: ${configNames}\n"

// 合并每个配置
configNames.each { name ->
    println "处理: ${name}"
    def merged = [:]
    
    // 1. devops 通用 (application-{name}.yml)
    def f1 = new File(devopsBase, "application-${name}.yml")
    if (f1.exists()) {
        merged = deepMerge(merged, loadYaml(f1))
        println "  [1] devops通用: ${f1.name}"
    }
    
    // 2. devops 环境 ({env}/application-{name}.yml)
    def f2 = new File(devopsBase, "${activeProfile}/application-${name}.yml")
    if (f2.exists()) {
        merged = deepMerge(merged, loadYaml(f2))
        println "  [2] devops-${activeProfile}: ${f2.name}"
    }
    
    // 3. API 业务 (config/application-{name}.yml)
    def f3 = new File(apiConfig, "application-${name}.yml")
    if (f3.exists()) {
        merged = deepMerge(merged, loadYaml(f3))
        println "  [3] API业务: ${f3.name}"
    }
    
    // 输出合并后的文件
    if (merged) {
        def out = new File(outputDir, "application-${name}.yml")
        saveYaml(merged, out)
        println "  => 输出: ${out.name}"
    }
}

println "\n=== 合并完成 ==="

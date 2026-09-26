package github.mkbaka.assiya.platform.bukkit.internal.config.loader

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.nodes.MappingNode
import org.yaml.snakeyaml.nodes.ScalarNode
import java.io.StringReader

object ConfigYaml {

    fun parse(
        default: Map<String, *>,
        comments: MutableMap<String, List<String>> = linkedMapOf()
    ): YamlConfiguration {
        val yaml = YamlConfiguration()

        writeDefaults(yaml, default, comments)

        return yaml
    }

    @Suppress("UNCHECKED_CAST")
    private fun writeDefaults(
        section: ConfigurationSection,
        default: Map<String, *>,
        comments: MutableMap<String, List<String>>,
        parent: String = ""
    ) {
        for ((key, value) in default) {
            val path = if (parent.isEmpty()) key else "$parent.$key"

            // 记录注释
            if (key.endsWith("-comments")) {
                val target = path.removeSuffix("-comments")
                comments[target] = value as List<String>

                continue
            }

            // 寻找要写入的父节点
            val parts = key.split('.')
            var target = section

            for (part in parts.dropLast(1)) {
                val existing = target.get(part)

                target = if (existing == null) {
                    target.createSection(part)
                } else {
                    existing as ConfigurationSection
                }
            }

            val name = parts.last()

            // 展开嵌套 Map
            if (value is Map<*, *>) {
                val existing = target.get(name)

                val child = if (existing == null) {
                    target.createSection(name)
                } else {
                    existing as ConfigurationSection
                }

                writeDefaults(child, value as Map<String, *>, comments, path)

                continue
            }

            check(!target.contains(name)) {
                "Duplicate configuration path: $path"
            }

            target.set(name, value)
        }
    }

    fun render(
        yaml: YamlConfiguration,
        comments: Map<String, List<String>>
    ): String {
        val text = yaml.saveToString()

        if (comments.isEmpty()) {
            return text
        }

        // 解析生成后的文本
        // 获取各节点在实际输出中的行号和列号.
        val root = Yaml().compose(StringReader(text)) as MappingNode
        val insertions = hashMapOf<Int, String>()

        // 把注释转换成行号对应的待插入文本.
        collectComments(root, "", comments, insertions)

        val result = StringBuilder()
        val lines = text.split('\n')

        // 插入注释
        for ((index, line) in lines.withIndex()) {
            val comment = insertions[index]

            if (comment != null) {
                result.append(comment)
            }

            result.append(line)

            if (index < lines.lastIndex) {
                result.append('\n')
            }
        }

        return result.toString()
    }

    private fun collectComments(
        node: MappingNode,
        parent: String,
        comments: Map<String, List<String>>,
        insertions: MutableMap<Int, String>
    ) {
        for (entry in node.value) {
            // 找出完整节点对应的注释
            val key = entry.keyNode as ScalarNode
            val path = if (parent.isEmpty()) key.value else "$parent.${key.value}"

            val comment = comments[path]

            if (comment != null) {
                // 缩进
                val indent = " ".repeat(key.startMark.column)
                val text = StringBuilder()

                for (description in comment) {
                    for (line in description.lines()) {
                        text.append(indent)
                        text.append("# ")
                        text.append(line)
                        text.append('\n')
                    }
                }

                insertions[key.startMark.line] = text.toString()
            }

            val child = entry.valueNode

            if (child is MappingNode) {
                collectComments(child, path, comments, insertions)
            }
        }
    }
}

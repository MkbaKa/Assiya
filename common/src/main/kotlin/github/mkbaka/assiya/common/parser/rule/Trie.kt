package github.mkbaka.assiya.common.parser.rule

class Trie private constructor(
    private val root: Node
) {

    fun find(line: String): List<Candidate> {
        val found = linkedSetOf<Candidate>()
        var activeNodes = emptyList<ActiveNode>()

        for ((index, char) in line.withIndex()) {
            val next = arrayListOf<ActiveNode>()

            fun append(parent: Node, start: Int) {
                val child = parent.children[char] ?: return
                next += ActiveNode(child, start)
                for (rule in child.rules) {
                    found += Candidate(rule, start)
                }
            }

            for (active in activeNodes) {
                append(active.node, active.startIndex)
            }

            append(root, index)

            activeNodes = next
        }

        return found.toList()
    }

    private data class ActiveNode(
        val node: Node,
        val startIndex: Int
    )

    private data class Node(
        val children: MutableMap<Char, Node> = hashMapOf(),
        val rules: MutableList<TextRule> = arrayListOf()
    )

    data class Candidate(
        val rule: TextRule,
        val startIndex: Int,
    )

    companion object {

        fun build(rules: List<TextRule>): Trie {
            val root = Node()

            for (rule in rules) {
                for (name in rule.names) {
                    var current = root
                    for (char in name) {
                        current = current.children.computeIfAbsent(char) { Node() }
                    }
                    current.rules += rule
                }
            }

            return Trie(root)
        }

    }

}
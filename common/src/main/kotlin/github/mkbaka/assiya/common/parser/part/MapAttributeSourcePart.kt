package github.mkbaka.assiya.common.parser.part

class MapAttributeSourcePart(
    val attributes: List<AttributeGroup>,
    val conditions: List<ConditionGroup> = emptyList()
) : AttributeSourcePart {

    override val type: String = TYPE

    class AttributeGroup(val id: String, val attributes: List<Attribute>)

    class Attribute(val id: String, val captures: Map<String, Any?>)

    class ConditionGroup(val paths: List<String>, val conditions: List<Condition>)

    class Condition(val id: String, val parameters: Map<String, Any?>)

    companion object {

        const val TYPE = "map"

        fun fromMap(attributes: Map<*, *>, conditions: List<*> = emptyList<Any>()): MapAttributeSourcePart {
            /**
             * attributes:
             *   bonus:
             *     attack:
             *       value: 10
             *       percent: 15
             *
             * 把以上 nbt 转成这种结构
             *
             * AttributeGroup{
             *    id: bonus,
             *    attributes: [
             *      Attribute{
             *        id: attack,
             *        captures: {
             *          value: 10,
             *          percent: 10
             *        }
             *      }
             *    ]
             * }
             */
            val groups = arrayListOf<AttributeGroup>()
            for ((group, rawAttributes) in attributes) {
                val rawGroup = rawAttributes as? Map<*, *> ?: continue
                val values = arrayListOf<Attribute>()
                for ((id, rawCaptures) in rawGroup) {
                    val captures = rawCaptures as? Map<*, *> ?: continue
                    val parameters = linkedMapOf<String, Any?>()
                    for ((key, value) in captures) {
                        if (key is String) parameters[key] = value
                    }
                    values += Attribute(id.toString(), parameters)
                }
                groups += AttributeGroup(group.toString(), values)
            }

            /**
             * conditions:
             *   - paths:
             *       - bonus.attack.percent
             *     conditions:
             *       - key: level
             *         min: 30
             *
             * ConditionGroup{
             *    paths: ["bonus.attack.percent"],
             *    conditions: [
             *      Condition{
             *        id: level,
             *        parameters: {
             *          key: level,
             *          min: 30
             *        }
             *      }
             *    ]
             * }
             */
            val records = arrayListOf<ConditionGroup>()
            for (rawRecord in conditions) {
                val record = rawRecord as? Map<*, *> ?: continue
                val paths = record["paths"] as? List<*> ?: continue
                val rawConditions = record["conditions"] as? List<*> ?: continue
                val entries = arrayListOf<Condition>()
                for (rawCondition in rawConditions) {
                    val data = rawCondition as? Map<*, *> ?: continue
                    val id = data["key"] as? String ?: continue
                    val parameters = linkedMapOf<String, Any?>()
                    for ((key, value) in data) {
                        if (key is String) parameters[key] = value
                    }
                    entries += Condition(id, parameters)
                }
                records += ConditionGroup(paths.map { it as String }, entries)
            }
            return MapAttributeSourcePart(groups, records)
        }

    }

}

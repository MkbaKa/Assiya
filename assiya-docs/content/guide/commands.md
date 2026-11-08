<script setup>
const commands = [
  {
    "path": [
      "status"
    ],
    "label": "查看玩家总属性值",
    "permission": "assiya.command.status",
    "description": "在聊天框里查看目标玩家的属性值总和。",
    "params": [
      {
        "key": "player",
        "input": "text",
        "initial": "Steve",
        "name": "玩家名",
        "type": "Player",
        "required": true,
        "description": "目标玩家名"
      }
    ]
  },
  {
    "path": [
      "source",
      "list"
    ],
    "label": "查询来源",
    "permission": "assiya.command.source.list",
    "description": "列出目标玩家拥有的属性来源。",
    "params": [
      {
        "key": "player",
        "input": "text",
        "initial": "Steve",
        "name": "玩家名",
        "type": "Player",
        "required": true,
        "description": "目标玩家名"
      }
    ]
  },
  {
    "path": [
      "source",
      "info"
    ],
    "label": "来源内容",
    "permission": "assiya.command.source.info",
    "description": "查看特定来源提供的属性值。",
    "params": [
      {
        "key": "player",
        "input": "text",
        "initial": "Steve",
        "name": "玩家名",
        "type": "Player",
        "required": true,
        "description": "目标玩家名"
      },
      {
        "key": "source",
        "input": "text",
        "initial": "assiya:equipment:main_hand",
        "presets": [
          {
            "label": "主手装备",
            "value": "assiya:equipment:main_hand"
          },
          {
            "label": "副手装备",
            "value": "assiya:equipment:off_hand"
          },
          {
            "label": "头盔槽位",
            "value": "assiya:equipment:head"
          },
          {
            "label": "胸甲槽位",
            "value": "assiya:equipment:chest"
          },
          {
            "label": "护腿槽位",
            "value": "assiya:equipment:legs"
          },
          {
            "label": "靴子槽位",
            "value": "assiya:equipment:feet"
          },
          {
            "label": "默认属性",
            "value": "assiya:default-attributes"
          },
          {
            "label": "自定义来源",
            "value": "custom"
          }
        ],
        "name": "来源名称",
        "type": "String",
        "required": true,
        "description": "来源名称，可以 TAB 补全"
      }
    ]
  },
  {
    "path": [
      "source",
      "give"
    ],
    "label": "给予属性",
    "permission": "assiya.command.source.give",
    "description": "给玩家一个自定义属性源。",
    "params": [
      {
        "key": "player",
        "input": "text",
        "initial": "Steve",
        "name": "玩家名",
        "type": "Player",
        "required": true,
        "description": "目标玩家名"
      },
      {
        "key": "source",
        "input": "text",
        "initial": "buff_1",
        "name": "来源名称",
        "type": "String",
        "required": true,
        "description": "同名会覆盖旧的属性源"
      },
      {
        "key": "priority",
        "input": "number",
        "initial": 100,
        "name": "优先级",
        "type": "Int",
        "required": true,
        "description": "属性源的优先级"
      },
      {
        "key": "attributes",
        "input": "text",
        "initial": "物理伤害100,暴击几率50",
        "name": "属性文本",
        "type": "String",
        "required": true,
        "description": "多条属性之间用英文逗号 <code>,</code> 分隔，不能包含空格"
      },
      {
        "key": "duration",
        "input": "number",
        "initial": -1,
        "name": "持续秒数",
        "type": "Long (秒)",
        "required": false,
        "default": -1,
        "description": "-1 代表持续到下线"
      },
      {
        "key": "silent",
        "input": "boolean",
        "initial": false,
        "name": "是否发送提示",
        "type": "Boolean",
        "required": false,
        "default": false,
        "description": "开启后，不会给执行者发给予属性的提示"
      }
    ]
  },
  {
    "path": [
      "source",
      "remove"
    ],
    "label": "移除属性源",
    "permission": "assiya.command.source.remove",
    "description": "删除指定来源。",
    "params": [
      {
        "key": "player",
        "input": "text",
        "initial": "Steve",
        "name": "玩家名",
        "type": "Player",
        "required": true,
        "description": "目标玩家名"
      },
      {
        "key": "source",
        "input": "text",
        "initial": "buff_1",
        "name": "来源名称",
        "type": "String",
        "required": true,
        "description": "要移除的属性源名称"
      },
      {
        "key": "silent",
        "input": "boolean",
        "initial": false,
        "name": "是否发送提示",
        "type": "Boolean",
        "required": false,
        "default": false,
        "description": "开启后，不会给执行者发移除来源的提示"
      }
    ]
  },
  {
    "path": [
      "reload"
    ],
    "label": "重载配置",
    "permission": "assiya.command.reload",
    "description": "重新加载插件的配置。",
    "params": [
      {
        "key": "module",
        "input": "select",
        "initial": "all",
        "options": [
          {
            "value": "all",
            "label": "all"
          },
          {
            "value": "script",
            "label": "script"
          },
          {
            "value": "reader",
            "label": "reader"
          },
          {
            "value": "formula",
            "label": "formula"
          },
          {
            "value": "condition",
            "label": "condition"
          },
          {
            "value": "attribute",
            "label": "attribute"
          },
          {
            "value": "mythicmobs",
            "label": "mythicmobs"
          }
        ],
        "name": "模块名",
        "type": "String",
        "required": false,
        "default": "all",
        "description": "不指定则默认全部重载"
      }
    ]
  }
]
</script>

# 命令

<CommandTree
  root="/assiya"
  :commands="commands"
  separator=" "
  suffix=""
  tree-title="命令结构"
  example-title="命令示例"
  params-title="命令参数"
  copy-label="复制命令"
/>

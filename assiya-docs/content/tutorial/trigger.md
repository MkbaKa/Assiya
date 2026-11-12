# 触发器

如果把属性比作需要通过安检才能进入的场所，那么触发器对应的就是场所大门与安检入口。

只有通过特定门进入的游客才能前往对应的场所。

## 触发器对应的函数名

|   触发器   | 共享优先级的触发器 |    函数名    |
|:-------:|:---------:|:---------:|
| attack  |  defense  | onAttack  |
| defense |  attack   | onDefense |
|  kill   |           |  onKill   |
| update  |           | onUpdate  |
|  timer  |           |  onTimer  |

## 对应的页面

- [attack & defense](trigger-attack.md)
- [kill](trigger-kill.md)
- [update](trigger-update.md)
- [timer](trigger-timer.md)

import DefaultTheme from 'vitepress/theme'
import Mermaid from './components/Mermaid.vue'
import ConditionDemo from './components/ConditionDemo.vue'
import FormulaDemo from './components/FormulaDemo.vue'
import ReaderDemo from './components/ReaderDemo.vue'
import RegexTester from './components/RegexTester.vue'
import McTooltip from './components/McTooltip.vue'
import DamageStepper from './components/DamageStepper.vue'
import ParamList from './components/ParamList.vue'
import TriggerCards from './components/TriggerCards.vue'
import SyntaxBar from './components/SyntaxBar.vue'
import FlowSteps from './components/FlowSteps.vue'
import AttrCatalog from './components/AttrCatalog.vue'
import ConditionCatalog from './components/ConditionCatalog.vue'
import MmLineBuilder from './components/MmLineBuilder.vue'
import CommandTree from './components/CommandTree.vue'
import './style.css'

export default {
  extends: DefaultTheme,
  enhanceApp({ app }) {
    app.component('ConditionDemo', ConditionDemo)
    app.component('FormulaDemo', FormulaDemo)
    app.component('Mermaid', Mermaid)
    app.component('ReaderDemo', ReaderDemo)
    app.component('RegexTester', RegexTester)
    app.component('McTooltip', McTooltip)
    app.component('DamageStepper', DamageStepper)
    app.component('ParamList', ParamList)
    app.component('TriggerCards', TriggerCards)
    app.component('SyntaxBar', SyntaxBar)
    app.component('FlowSteps', FlowSteps)
    app.component('AttrCatalog', AttrCatalog)
    app.component('ConditionCatalog', ConditionCatalog)
    app.component('MmLineBuilder', MmLineBuilder)
    app.component('CommandTree', CommandTree)
  }
}

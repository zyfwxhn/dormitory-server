# Dormitory Management System — CLAUDE.md

## 项目 skills

本项目在 `.claude/skills/` 下配置了 8 个 skill，Code 模块中使用 `/skill名` 调用：

| Skill | 用途 | 触发场景 |
|-------|------|----------|
| `/brainstorm` | 头脑风暴/选题构思 | 需要构思方案、讨论方向 |
| `/review` | 代码审查 | 提交前 review 代码变更 |
| `/investigate` | 系统化调试 | bug 排查、报错定位 |
| `/qa` | 测试用例编写 | 写单元/集成/E2E 测试 |
| `/tdd` | 测试驱动开发 | 先写测试再写代码 |
| `/verify` | 完成前验证 | 声称完成前验证正确性 |
| `/handoff` | 会话交接 | 结束会话前保存上下文 |
| `/web-access` | 智能联网 | 搜索、浏览网页、抓取内容 |

## 推荐开发工作流

```
/writing-plans 拆任务 → /tdd 写测试 → 写代码 → /review 审查 → /qa 补测试 → /verify 验证 → /handoff 交接
```

## 技术栈

- 后端：Node.js / Express
- 前端：Vue
- 数据库：MySQL

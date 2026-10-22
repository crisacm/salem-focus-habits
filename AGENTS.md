## Memory
<!-- snippet-version: 1.0.0 -->

You have access to Engram persistent memory via MCP (mem_save, mem_search, mem_session_summary, etc.).

- Proactively save after significant work, without waiting to be asked.
- After any context compaction or reset, first persist the injected summary using mem_session_summary. Request mem_context only if more context is needed.

## Workflow
<!-- snippet-version: 1.0.0 -->

Before any coding task, evaluate whether the odd (normal work, the default methodology) or rdd (only if asked to review something already done) skill applies. Default to the `-lite` version (odd-lite, rdd-lite) and simply follow it, without announcing it.

If the complexity or risk of the task suggests the `-full` version, do NOT choose it on your own: ask the user which one to use before starting, recommend one, and wait for the answer. Keep the question short, stating what each option implies:

- odd: both versions always delegate exploration to the `explorer` subagent. Lite works directly afterwards (one task doc if substantial); full also resolves real uncertainty explicitly, uses TDD and plans delivery for large diffs (~400+ lines). Recommend full for changes with real uncertainty or likely large.
- rdd: lite reviews in the same session; full delegates medium/high-risk reviews to a `fresh-reviewer` subagent that only sees the diff (no self-review bias; more tokens). Recommend full for business logic, auth, payments, migrations or infra.

odd (both versions) needs the `explorer` agent installed; `rdd-full` needs `fresh-reviewer`. If an agent is missing, tell the user before continuing (for rdd, use `rdd-lite`). rdd is never started on your own: it requires an explicit request or the user's acceptance of your end-of-task offer.

When an odd task finishes, offer an RDD review in one short question: after odd-lite, offer a quick review (`rdd-lite`); after odd-full, offer a quick (`rdd-lite`) or deep (`rdd-full`) review, recommending the deep one for auth, payments, migrations or infra. Run it only if the user accepts.

## Code exploration
<!-- snippet-version: 1.0.0 -->

You have access to the codebase-memory-mcp knowledge graph (search_graph, trace_path, get_code_snippet, query_graph, get_architecture, detect_changes, etc.). Use it autonomously, without waiting to be asked, BEFORE reading or grepping files by hand when you need to:

- Explore or understand the codebase: architecture, structure, where something is defined.
- Follow relationships: who calls a function, what it calls, call chains, dependencies.
- Assess the impact of a change: before refactors or edits that touch shared code, and detect_changes over the current diff.
- Audit code quality: dead code, high fan-out, refactor candidates.

Use grep/search_code only for literals or when the graph does not cover the path (check_index_coverage). Skip the graph for trivial, single-file edits where the location is already known.

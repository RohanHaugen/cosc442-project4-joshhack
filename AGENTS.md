# AGENTS.md: JoshHack Repository Instructions
## Environment
- Java 17, Maven (use the Maven Wrapper: `./mvnw`, or `mvnw.cmd` on Windows)
- Tests: JUnit 5, located in `src/test/java`
- Production code: `src/main/java`
- CI: GitHub Actions (JUnit, then PIT, then SpotBugs/SARIF upload)

## Quality checks required before work is considered complete
1. `./mvnw test` must pass (full JUnit suite)
2. PIT mutation testing: `./mvnw.cmd test-compile org.pitest:pitest-maven:mutationCoverage` when tests or covered code changed
3. SpotBugs: `./mvnw compile spotbugs:spotbugs` when production code changed
4. Report the commands run and their results.

## Rules
- Preserve existing behavior unless a change is explicitly requested.
- Never weaken, remove, or disable tests (including `@Disabled`, loosened assertions, or removed assertions) just to make the build pass.
- Do not change PIT, SpotBugs, or CI configuration to hide problems.
- Production-code changes (`src/main/java`) must be clearly identified and left for human review. Describe what changed and why.
- Never push, merge, or open pull requests automatically. Leave commits to the human unless explicitly told otherwise.
- Keep changes small and in scope; do not touch unrelated files.
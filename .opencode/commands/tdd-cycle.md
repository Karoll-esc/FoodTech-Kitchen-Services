---
name: tdd-cycle
description: Implement feature using TDD with RED-GREEN-REFACTOR approval gates
arguments:
  - name: feature
    description: Feature or component to implement
    required: true
context_files:
  - AGENTS.md
  - readme.md
---
# TDD Cycle with Approval Gates
Implement **{{feature}}** following the RED-GREEN-REFACTOR cycle with explicit approval gates between each phase.
## Protocol
### Phase 1: RED (Failing Tests)
**Objective:** Define all business rules through failing tests
1. **Analyze requirements** for {{feature}}
2. **Create comprehensive unit tests** that cover:
   - Happy path scenarios
   - Edge cases
   - Error conditions
   - Null/invalid input validation
   - All business rules
3. **Ensure all tests FAIL** (implementation doesn't exist yet)
4. **Run tests** and capture failure output
5. **Present results** in this format:
```
=== RED PHASE RESULTS ===
Created [N] unit tests for [ComponentName]:
✅ Test 1: description (e.g., "Bar staff authorized for BAR station")
✅ Test 2: description
✅ Test 3: description
...
Files Created/Modified:
- src/test/java/path/to/ComponentTest.java [NEW] - N tests
Test Results: [N] tests FAILING ❌ (expected - implementation doesn't exist)
✅ RED phase complete.
```
6. **STOP and ASK:** "Should I proceed to GREEN phase?"
7. **WAIT for explicit user approval** ("yes", "proceed", "go ahead", etc.) before continuing
---
### Phase 2: GREEN (Minimal Implementation)
**Objective:** Write just enough code to make tests pass
**⚠️ Only proceed after user approval from RED phase**
1. **Implement minimal code** to make all tests pass:
   - Create necessary classes/methods
   - Focus on functionality, NOT code quality
   - No optimization, no fancy error messages yet
   - Just make it work
2. **Register beans** if needed (Spring @Bean in config)
3. **Run all tests** to verify they pass
4. **Present results** in this format:
```
=== GREEN PHASE RESULTS ===
Implemented minimal code for {{feature}}:
Files Created:
- src/main/java/path/to/Component.java [NEW] - Core logic
- src/main/java/path/to/CustomException.java [NEW] - Exception class
Files Modified:
- src/main/java/path/to/Controller.java [MODIFIED] - Integrated component
- src/main/java/path/to/Config.java [MODIFIED] - Registered bean
Key Implementation:
- Component.method() - Core business logic
- Basic exception throwing for validation
Test Results: [Total] tests PASSING ✅ ([N] new + [M] existing)
✅ GREEN phase complete.
```
5. **STOP and ASK:** "Should I proceed to REFACTOR phase?"
6. **WAIT for explicit user approval** before continuing
---
### Phase 3: REFACTOR (Code Quality)
**Objective:** Improve code quality while keeping tests green
**⚠️ Only proceed after user approval from GREEN phase**
1. **Enhance code quality** with these improvements:
   - ✅ Add **null validation** with IllegalArgumentException
   - ✅ Enhance **error messages** with detailed context
   - ✅ Add **comprehensive JavaDoc** (class and method level)
   - ✅ Extract **helper methods** if any method > 20 lines
   - ✅ Apply **SOLID principles** (especially SRP)
   - ✅ Use **explicit types** (no var in domain/application)
   - ✅ Add **defensive copies** for collection getters
   - ✅ Remove any **commented code** or TODOs
   - ✅ Ensure **descriptive names** (no abbreviations)
2. **Run tests after EACH refactoring** to ensure they stay green
3. **Present results** in this format:
```
=== REFACTOR PHASE RESULTS ===
Code Quality Improvements:
1. Enhanced [ClassName]:
   ✅ Added null validation for parameters
   ✅ Enhanced error messages with user context
   ✅ Added comprehensive JavaDoc with usage examples
   ✅ Extracted helper method: methodName()
2. Improved [OtherClass]:
   ✅ Applied SRP - separated concerns
   ✅ Added defensive copies for collection getters
   ✅ Used explicit types (replaced var)
Files Modified:
- src/main/java/path/to/Component.java [REFACTORED]
- src/main/java/path/to/CustomException.java [REFACTORED]
Code Quality Metrics:
- All methods < 20 lines ✅
- No commented code ✅
- Descriptive names ✅
- SOLID principles applied ✅
- Null validation present ✅
Test Results: [Total] tests PASSING ✅ (no regressions)
✅ REFACTOR phase complete. Code is production-ready.
```
4. **STOP and ASK:** "Ready to commit changes?"
---
## Critical Rules (NON-NEGOTIABLE)
### Approval Gates
- ❌ NEVER skip phases (must go RED → GREEN → REFACTOR in order)
- ❌ NEVER proceed to next phase without explicit user approval
- ❌ NEVER combine phases (e.g., write tests + implementation together)
- ✅ ALWAYS wait for user to say "yes", "proceed", or "go ahead"
- ✅ ALWAYS show test results at end of each phase
- ✅ ALWAYS run tests before asking for approval
### Testing Standards
- Use @DisplayName with Spanish descriptions for business context
- Test method names: should[ExpectedBehavior]When[Condition]
- Use Given-When-Then structure in test body
- Cover all business rules, not just happy path
- Minimum test count: Based on business rules (typically 5-10 tests)
### Architecture Compliance
- Follow Hexagonal Architecture (domain → application → infrastructure)
- NO Spring annotations in domain/application layers
- Apply SOLID principles (especially SRP)
- Keep methods under 20 lines
- Use explicit type declarations (no var in domain/application)
- Add defensive copies for collection getters
---
## Example Session Flow
User: /tdd-cycle station-based authorization
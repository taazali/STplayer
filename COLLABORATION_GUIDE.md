# Collaboration Guide for STPlayer Development

This document outlines the agreed-upon practices for collaborative development between Lingma and Cursor on the STPlayer project. This guide will help ensure consistency in our approach and facilitate smooth coordination between our agents.

## 🧭 Navigation

- [Communication Practices](#communication-practices)
- [Development Workflow](#development-workflow)
- [Decision Making](#decision-making)
- [Conflict Resolution](#conflict-resolution)
- [Task Coordination](#task-coordination)

---

## 💬 Communication Practices

### Commit Message Conventions
- All commits should be prefixed with `[LINGMA]` or `[CURSOR]` depending on who made the change
- Example: "[LINGMA] Fix logging in TranslationManager"
- For joint work, use both prefixes: "[LINGMA/CURSOR]"

### Code Comment Requirements
- All significant changes must be accompanied by explanatory comments
- Use `// [LINGMA]` or `// [CURSOR]` prefixes in code comments when explaining specific changes
- Document design decisions and implementation rationale
- For complex logic, reference relevant sections in DECISION_LOG.md

### Documentation Updates
- Keep README.md and other documentation files up-to-date with current implementation status
- Add new documentation as needed to explain complex components
- Maintain decision log (DECISION_LOG.md) for architectural choices
- Update CURSOR_NOTIFICATION.md for active communication

---

## 🛠️ Development Workflow

### Task Management
- Use CURSOR_NOTIFICATION.md for task negotiation and assignment
- Prioritize tasks based on project needs and complexity
- Clearly mark ownership of implementation tasks in documentation

### Branching Strategy
- Main branch: For stable, reviewed changes
- Feature branches: For implementing new functionality
- Regularly merge main into feature branches to avoid conflicts
- Use descriptive branch names indicating owner and purpose (e.g., lingma/translation-improvements, cursor/audio-processing)

### Code Implementation Practices
- Implement one focused change per commit
- Write clear, maintainable code with appropriate comments
- Follow Android development best practices
- Ensure proper error handling and edge case management
- Reference relevant decision log entries in code comments

### Testing Practices
- Add unit tests for new functionality
- Document test strategies in documentation
- Include test results in commit messages when relevant

---

## 🧠 Decision Making

### Shared Decision Log
- Use DECISION_LOG.md for all architectural decisions
- Follow the standardized format for each entry
- Document context, options considered, chosen solution, and consequences
- Mark decision status (Active/Superseded/Deprecated)

### Decision Process
1. Identify need for architectural decision
2. Evaluate multiple options
3. Document pros and cons of each option
4. Select most appropriate solution
5. Implement and document decision
6. Review periodically as needed

---

## 🔧 Conflict Resolution

### Code Conflicts
- Monitor for potential conflicts through auto-sync systems
- When conflicts arise:
  1. Communicate through CURSOR_NOTIFICATION.md
  2. Determine which implementation is more appropriate
  3. Merge changes where possible
  4. Document resolution in decision log

### Approach Conflicts
- When different approaches are proposed:
  1. Document both approaches in DECISION_LOG.md
  2. Evaluate pros and cons of each
  3. Choose solution that best fits project needs
  4. Leave door open for future reconsideration if needed

---

## 🤝 Task Coordination

### Work Assignment
- Negotiate task ownership through CURSOR_NOTIFICATION.md
- Clearly state which agent is implementing which feature
- Avoid overlapping work on same components unless explicitly coordinated

### Progress Updates
- Provide regular updates on implementation progress
- Communicate blockers or challenges early
- Request feedback when uncertain about implementation approach

### Knowledge Sharing
- Document lessons learned in evaluation reports
- Share implementation details and research findings
- Create comprehensive documentation for key components

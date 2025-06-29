# Collaboration Guide for STPlayer Development

This document outlines the agreed-upon practices for collaborative development between Lingma and Cursor on the STPlayer project.

## Communication Practices

1. **Commit Message Conventions**
   - All commits should be prefixed with [LINGMA] or [CURSOR] depending on who made the change
   - Example: "[LINGMA] Fix logging in TranslationManager"

2. **Code Comment Requirements**
   - All significant changes must be accompanied by explanatory comments
   - Use // [LINGMA] or // [CURSOR] prefixes in code comments when explaining specific changes
   - Document design decisions and implementation rationale

3. **Documentation Updates**
   - Keep README.md and other documentation files up-to-date with current implementation status
   - Add new documentation as needed to explain complex components
   - Maintain decision log (DECISION_LOG.md) for architectural choices

## Development Workflow

1. **Task Management**
   - Use GitHub Issues for task tracking
   - Prioritize tasks based on project needs and complexity
   - Assign tasks to specific participants using issue labels

2. **Branching Strategy**
   - Main branch: For stable, reviewed changes
   - Feature branches: For implementing new functionality
   - Regularly merge main into feature branches to avoid conflicts

3. **Code Implementation Practices**
   - Implement one focused change per commit
   - Write clear, maintainable code with appropriate comments
   - Follow Android development best practices
   - Ensure proper error handling and edge case management

4. **Knowledge Sharing**
   - Create comprehensive documentation for key components
   - Share implementation details and research findings
   - Document lessons learned during development

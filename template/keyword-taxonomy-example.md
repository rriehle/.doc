# Keyword Taxonomy

Example taxonomy for documentation keywords. Copy this to your project's
`doc-tools/keyword-taxonomy.md` and customize for your needs.

## Architecture & Design

- `:architecture` - Architectural decisions and patterns
- `:design-pattern` - Specific design patterns used
- `:modularity` - Module organization and boundaries
- `:layering` - Layered architecture concepts
- `:abstraction` - Abstraction layers and interfaces
- `:separation-of-concerns` - SoC principles
- `:coupling` - Component coupling considerations
- `:cohesion` - Component cohesion principles

## Security

- `:security` - General security concerns
- `:authentication` - Authentication mechanisms
- `:authorization` - Authorization and access control
- `:encryption` - Encryption and cryptography
- `:security-audit` - Security auditing
- `:vulnerability` - Security vulnerabilities
- `:threat-model` - Threat modeling
- `:compliance` - Regulatory compliance (GDPR, HIPAA, etc.)

## Performance & Scalability

- `:performance` - Performance optimization
- `:scalability` - Scalability considerations
- `:latency` - Latency optimization
- `:throughput` - Throughput optimization
- `:caching` - Caching strategies
- `:load-balancing` - Load balancing
- `:horizontal-scaling` - Horizontal scaling patterns
- `:vertical-scaling` - Vertical scaling patterns

## API & Integration

- `:api` - API design and implementation
- `:rest` - REST API design
- `:graphql` - GraphQL APIs
- `:grpc` - gRPC services
- `:websocket` - WebSocket connections
- `:api-versioning` - API versioning strategies
- `:backwards-compatibility` - API compatibility
- `:integration` - System integration patterns

## Data & Storage

- `:database` - Database design and usage
- `:sql` - SQL databases
- `:nosql` - NoSQL databases
- `:schema` - Database schema design
- `:migration` - Data migrations
- `:replication` - Data replication
- `:backup` - Backup strategies
- `:data-consistency` - Data consistency models

## Testing & Quality

- `:testing` - Testing strategies
- `:unit-test` - Unit testing
- `:integration-test` - Integration testing
- `:e2e-test` - End-to-end testing
- `:property-test` - Property-based testing
- `:test-coverage` - Test coverage
- `:tdd` - Test-driven development
- `:testability` - Testability design

## DevOps & Operations

- `:deployment` - Deployment strategies
- `:ci-cd` - CI/CD pipelines
- `:monitoring` - System monitoring
- `:observability` - Observability practices
- `:logging` - Logging strategies
- `:metrics` - Metrics collection
- `:tracing` - Distributed tracing
- `:alerting` - Alerting strategies

## Error Handling & Reliability

- `:error-handling` - Error handling patterns
- `:fault-tolerance` - Fault tolerance
- `:resilience` - System resilience
- `:circuit-breaker` - Circuit breaker pattern
- `:retry` - Retry strategies
- `:graceful-degradation` - Graceful degradation
- `:failover` - Failover mechanisms

## Documentation

- `:documentation` - Documentation practices
- `:guide` - User guides
- `:tutorial` - Tutorials
- `:reference` - Reference documentation
- `:runbook` - Operational runbooks
- `:onboarding` - Onboarding documentation

## Development Process

- `:workflow` - Development workflows
- `:code-review` - Code review practices
- `:refactoring` - Refactoring strategies
- `:technical-debt` - Technical debt management
- `:legacy` - Legacy system handling
- `:migration` - System migrations

## Domain-Specific

Add your project-specific keywords here:

- `:your-domain-concept` - Description
- `:your-feature` - Description
- `:your-component` - Description

## Cross-References

These keywords help link to other dev meta utilities:

- `:adr-ref` - References an Architecture Decision Record
- `:req-ref` - References a requirement
- `:runnote-ref` - References development notes

## Usage Guidelines

1. **Consistency**: Use established keywords when possible
2. **Specificity**: Prefer specific keywords over generic ones
3. **Kebab-case**: Always use kebab-case for multi-word keywords
4. **Avoid Redundancy**: Don't use both `:api` and `:apis`
5. **Document New Keywords**: Add descriptions for project-specific keywords
6. **Review Periodically**: Update taxonomy as project evolves

## Taxonomy Maintenance

- Review quarterly for new keywords needed
- Consolidate synonyms and duplicates
- Remove unused keywords after 6 months
- Keep descriptions up-to-date
- Align with team vocabulary

## See Also

- Project-specific taxonomy additions
- Team conventions documentation
- Related ADRs on documentation standards

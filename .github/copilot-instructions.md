# Workspace setup

- [x] Requirements clarified: independent Java 17 / Spring Boot 3 microservice.
- [x] Project scaffolded in the workspace root.
- [x] HU1-HU12 implemented across the independent Usuarios and Plazoleta services, with shared Bearer HMAC authentication.
- [x] No extensions were specified by the setup guidance.
- [x] `mvn test` passes.
- [x] No custom VS Code task is needed; Maven commands are documented in README.
- [ ] Launch the application only after the user confirms.
- [x] README and project documentation are complete.

## Project conventions

- Keep this as the independent Plazoleta service; do not merge or overwrite the Usuarios repository.
- Use Java 17, Spring Boot 3.5.x, Maven, and the existing package layers under `com.plazoleta`.
- Follow `entity`, `dto/request`, `dto/response`, `service` and `service/impl` package conventions.
- Use MySQL for local runtime and H2 only for automated tests.
- Require the shared `AUTH_SECRET` for cross-service tokens in any deployment; never commit real secrets.
- Validate ownership in the service layer and never trust client-supplied role or user-ID headers.
- Keep HU4 updates limited to price and description.
- Run `mvn test` before integrating changes into `develop`.

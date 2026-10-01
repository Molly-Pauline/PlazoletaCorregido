# Workspace setup

- [x] Requirements clarified: independent Java 17 / Spring Boot 3 microservice.
- [x] Project scaffolded in the workspace root.
- [x] HU2-HU4 implemented with tests; HU5 Bearer HMAC verification consumes tokens from Usuarios.
- [x] No extensions were specified by the setup guidance.
- [x] `mvn test` passes.
- [x] No custom VS Code task is needed; Maven commands are documented in README.
- [ ] Launch the application only after the user confirms.
- [x] README and project documentation are complete.

## Project conventions

- Keep this as the independent Plazoleta service; do not merge or overwrite the Usuarios repository.
- Use Java 17, Spring Boot 3.5.x, Maven, and the existing package layers under `com.plazoleta`.
- Require the shared `AUTH_SECRET` for cross-service tokens in any deployment; never commit real secrets.
- Validate ownership in the service layer and never trust client-supplied role or user-ID headers.
- Keep HU4 updates limited to price and description.
- Run `mvn test` before integrating changes into `develop`.

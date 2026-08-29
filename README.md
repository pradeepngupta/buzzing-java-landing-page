# Buzzing Java landing page

A static-first book launch page for **Buzzing Java: The Hidden Reason Java Remains the Foundation of Trusted Systems in the Age of AI**. It is authored with Java 21, Spring Boot 3, Thymeleaf, semantic HTML, modern CSS, and minimal vanilla JavaScript.

## Run locally

Prerequisites: Java 21 and Maven 3.8+.

```powershell
mvn spring-boot:run
```

Open `http://localhost:8080`. Run the tests with:

```powershell
mvn test
```

## Generate static HTML

Build the application, then run the export profile as a non-web process:

```powershell
mvn package
java -jar target/buzzing-java-landing-page-0.1.0-SNAPSHOT.jar --spring.profiles.active=export --spring.main.web-application-type=none
```

The standalone site is written to `dist/` with `index.html`, `css/`, `js/`, `images/`, and `fonts/`. It can be opened from a static host without Spring Boot. The exporter renders the same Thymeleaf template and copies classpath assets.

## CI/CD

GitHub Actions runs `mvn verify` for pushes and pull requests. Pushes to `main` or `master` also export the standalone site and deploy `dist/` to GitHub Pages, then trigger a Spring Boot deployment on Render.

To enable the deployments:

1. Set the repository's **Pages** source to **GitHub Actions**.
2. Create a Render Web Service for this project using the Docker deployment option.
3. Connect the repo and use the included `Dockerfile` as the build configuration. Render will expose the app on the `PORT` environment variable automatically.
4. Create a Render deploy hook and save its URL as the GitHub repository secret `RENDER_DEPLOY_HOOK_URL`.

Render supplies the `PORT` environment variable; the application uses it automatically and falls back to port `8080` locally. Dockerized deployment should keep the app listening on `$PORT`, which Spring Boot reads from `application.yml`. Pull requests run CI only and do not deploy.

## Configuration

All page content is in `src/main/resources/application.yml`, bound to the typed `SiteProperties` model. The page does not scatter book details through templates.

- `cta.mode` supports `waitlist` and is structured for a future `buy` mode; all CTA instances use the shared label and link.
- The waitlist counter is loaded from the Google Sheet through the cached waitlist count endpoint.
- Set `freebie.enabled` to true and provide its configured title, description, and quantity to render the optional first-X section. It is disabled by default because the freebie is not decided yet.
- FAQ visible text and FAQPage JSON-LD are generated from the same `faq` list.
- Launch event schema is controlled by `launch-event.enabled`; unknown location data is intentionally omitted.

## Architecture and future work

The current flow is `Spring Boot + Thymeleaf -> responsive landing page -> static dist/ export`. The waitlist validates the request, checks the configured Google Sheet email column for duplicates, and appends new signups to the sheet.

The waitlist API is available at `GET /api/waitlist/count`, which returns the real non-empty email-row count with a 60-second in-memory cache, and `POST /api/waitlist`, which returns `{ "message": "You are on the list!" }` for both new and duplicate signups.

Not implemented yet: database, PostgreSQL, JPA, migrations, n8n, email automation, persistence, deployment, CloudFront, Render, infrastructure, CI/CD, DNS, and production secrets.

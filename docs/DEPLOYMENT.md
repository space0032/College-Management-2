# Production deployment

The production web application uses two services:

- **Render** runs the Java API from the root `Dockerfile`.
- **Netlify** builds and serves the React application using `netlify.toml`.
- **Supabase/PostgreSQL** stores production data.

## 1. Render API

Create a Render **Web Service**, connect this repository, and select the `Supabase` branch.

| Setting | Value |
| --- | --- |
| Runtime | Docker |
| Dockerfile | `./Dockerfile` |
| Health check | `/` |
| Root directory | Empty (repository root) |

Add these runtime environment variables:

```text
DB_URL=jdbc:postgresql://YOUR_HOST:5432/postgres?sslmode=require
DB_USER=YOUR_DATABASE_USER
DB_PASSWORD=YOUR_DATABASE_PASSWORD
H2_ENABLED=false
API_TOKEN_EXPIRATION_MS=86400000
CORS_ALLOWED_ORIGINS=https://YOUR_NETLIFY_SITE.netlify.app
```

Render supplies `PORT` automatically. Do not set it unless the service configuration specifically requires an override.

After deployment, opening the Render URL should return the API health response. Keep the generated HTTPS URL for the frontend configuration.

## 2. Netlify frontend

Import the same repository into Netlify and select the `Supabase` branch. The checked-in `netlify.toml` supplies:

```text
Base directory: web-app
Build command: npm run build
Publish directory: build
```

Add this build environment variable in Netlify:

```text
REACT_APP_API_URL=https://YOUR_RENDER_SERVICE.onrender.com/api
```

Trigger a new deployment after adding or changing the variable. React environment variables are embedded at build time.

## 3. Final CORS update

Once Netlify assigns the production domain, copy the exact origin into Render:

```text
CORS_ALLOWED_ORIGINS=https://YOUR_NETLIFY_SITE.netlify.app
```

For multiple frontend domains, use a comma-separated value with no trailing slashes:

```text
CORS_ALLOWED_ORIGINS=https://site.netlify.app,https://college.example.com
```

Save the Render environment and redeploy the API.

## Production notes

- Never add database credentials to `REACT_APP_*`; browser users can inspect those values.
- Disable H2 on Render because container-local storage is ephemeral.
- API login tokens are stored in process memory and are invalidated by a deployment or service restart.
- Files written to local `uploads/` are not durable on ephemeral hosting. Use object storage for production uploads.
- Container logs are written to stdout (`LOG_TO_FILE=false`) so they appear in the Render log stream.

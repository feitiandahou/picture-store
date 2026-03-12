# Picture Store

Picture Store is a full-stack image management platform built with Spring Boot and Vue 3. It supports personal and team image spaces, picture upload and batch import, image search, permission control, moderation, and space analytics.

The repository is split into:

- `picture-store-backend`: Spring Boot backend, REST API, authentication, storage integration, analytics, and persistence.
- `picture-store-frontend`: Vue 3 + Vite frontend for end users and administrators.

## Features

### User Features

- User registration and login
- Personal picture management
- Single picture upload and batch picture upload
- Picture metadata editing: name, introduction, category, tags
- Picture detail view
- Search pictures by keyword, color, or similar image
- Create personal spaces and browse your own spaces

### Team and Space Features

- Create private and team spaces
- Space-level quotas for picture count and total size
- Team member roles: viewer, editor, admin
- Space member management
- Space analytics dashboards for usage, category, tag, size, ranking, and user distribution

### Admin Features

- User management
- Picture moderation and review workflow
- Global picture management
- Space management across the platform

### Media and AI Features

- Tencent COS object storage integration
- Image crop/edit support in the frontend
- AI outpainting support through Aliyun AI
- Picture dominant color analysis and color-based retrieval

## Tech Stack

### Backend

- Java 8
- Spring Boot 2.7.6
- MyBatis-Plus
- MySQL
- Redis
- Sa-Token
- ShardingSphere
- Knife4j / OpenAPI
- Tencent COS SDK
- Jsoup
- Caffeine

### Frontend

- Vue 3
- TypeScript
- Vite
- Ant Design Vue
- Pinia
- Vue Router
- Axios
- ECharts
- vue-cropper

## Project Structure

```text
picture-store/
├─ picture-store-backend/     # Spring Boot backend
│  ├─ src/main/java/          # business code
│  └─ src/main/resources/     # config, SQL, mapper XML, static resources
├─ picture-store-frontend/    # Vue 3 frontend
│  ├─ src/api/                # generated API clients
│  ├─ src/components/         # reusable components
│  ├─ src/pages/              # page-level views
│  ├─ src/router/             # route definitions
│  └─ src/stores/             # Pinia stores
└─ readme.md
```

## Main Modules

### Backend Modules

- `UserController`: registration, login, current user, admin user management
- `PictureController`: upload, batch upload, query, edit, review, search, AI outpainting
- `SpaceController`: space creation, querying, detail, and management
- `SpaceUserController`: team member management and role assignment
- `SpaceAnalyzeController`: analytics for space usage and content distribution
- `TestCosController`: COS-related testing endpoints

### Frontend Pages

- Home page
- Login and register pages
- Add picture / batch add picture
- Picture detail page
- Search picture page
- Add space / my space / space detail
- Space analytics page
- Admin pages for users, pictures, spaces, and space members

## Architecture Notes

- Backend default port: `8123`
- Backend context path: `/api`
- Frontend development requests target: `http://localhost:8123`
- API docs: `http://localhost:8123/api/doc.html`
- OpenAPI schema for frontend generation: `http://localhost:8123/api/v2/api-docs`
- Picture metadata is stored in MySQL
- Session state is stored in Redis
- Picture files are stored in Tencent COS
- The picture table supports sharding by `spaceId`

## Prerequisites

Install the following locally before starting the project:

- JDK 8
- Maven 3.8+
- Node.js 18+ and npm
- MySQL 8+
- Redis 6+

## Database Initialization

1. Create a MySQL database named `picture_store`.
2. Run the SQL script below:

```sql
picture-store-backend/src/main/resources/create_table.sql
```

This script creates the following core tables:

- `user`
- `picture`
- `space`
- `space_user`

## Backend Setup

### 1. Configure application settings

Edit the backend configuration files and replace local credentials with your own values:

- `picture-store-backend/src/main/resources/application.yml`
- `picture-store-backend/src/main/resources/application-local.yml`

The project expects configuration for:

- MySQL datasource
- Redis
- Tencent COS
- Aliyun AI

Recommended local values:

- MySQL database: `picture_store`
- Redis host: `127.0.0.1`
- Redis port: `6379`
- Server port: `8123`

Important:

- Do not commit real secret keys or API keys.
- If secrets have already been committed, rotate them before using the project in any shared environment.

### 2. Start the backend

```bash
cd picture-store-backend
mvn spring-boot:run
```

After startup, verify:

- API base URL: `http://localhost:8123/api`
- API docs: `http://localhost:8123/api/doc.html`

## Frontend Setup

### 1. Install dependencies

```bash
cd picture-store-frontend
npm install
```

### 2. Start the development server

```bash
npm run dev
```

### 3. Build for production

```bash
npm run build
```

### 4. Lint the frontend

```bash
npm run lint
```

## Frontend Scripts

- `npm run dev`: start Vite dev server
- `npm run build`: type-check and build
- `npm run build-only`: build only
- `npm run preview`: preview production build
- `npm run lint`: run ESLint with auto-fix
- `npm run format`: format source files
- `npm run openapi`: regenerate frontend API clients from backend OpenAPI docs

## Backend Configuration Summary

The backend currently includes support for:

- Spring MVC REST APIs
- MyBatis-Plus and XML mappers
- Redis-backed session storage
- Sa-Token authentication and authorization
- COS file upload and processing
- Local cache with Caffeine
- ShardingSphere table sharding for pictures

## Permission Model

### Platform Roles

- `user`
- `admin`

### Space Roles

- `viewer`
- `editor`
- `admin`

## Analytics Capabilities

The project includes analytics pages and backend APIs for:

- Space usage
- Picture category distribution
- Picture tag distribution
- Space size analysis
- User contribution analysis
- Ranking analysis

## Development Notes

- The frontend request client uses cookies and sends requests with credentials enabled.
- The frontend API layer can be regenerated from the backend Swagger/OpenAPI endpoint.
- The backend enables SQL logging in the current configuration, which is useful for local debugging but should be disabled in production.
- The backend uses logical deletion for core entities.

## Suggested Local Startup Order

1. Start MySQL
2. Start Redis
3. Initialize the database with `create_table.sql`
4. Start `picture-store-backend`
5. Start `picture-store-frontend`
6. Open the frontend in your browser

## Future Improvements

- Move secrets out of local YAML files into environment variables
- Add Docker and docker-compose support
- Add automated backend and frontend test documentation
- Add deployment instructions for production environments

## License

No license file is currently included in this repository. Add one before distributing the project externally.
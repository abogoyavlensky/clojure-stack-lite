# Clojure Stack Lite

A quick way to start a full-stack Clojure app with server-side rendering. 
Built on a powerful yet lightweight stack featuring SQLite/PostgreSQL, HTMX, AlpineJS, and TailwindCSS v4.

This template is designed to be lightweight and easy to use, with a focus on rapid development and deployment. Minimal distraction and sane defaults. Everything is streamlined to give you a solid foundation for building modern web applications.

Get started building your new Clojure application in seconds and be productive!

## Usage

1. Create a new Clojure project using Clojure CLI:
   ```bash
   clojure -Ttools install-latest :lib io.github.seancorfield/deps-new :as new
   clojure -Tnew create :template io.github.abogoyavlensky/clojure-stack-lite :name myproject
   ```
    
    > Requires Clojure CLI tools version `1.12.0.1479` or later.
    
   Or alternatively using [neil](https://github.com/babashka/neil):

   ```bash
   brew install babashka/brew/neil
   neil new io.github.abogoyavlensky/clojure-stack-lite myproject
   ```

   Or using [frame](https://github.com/abogoyavlensky/frame), which needs no Clojure CLI and prompts for each option:

   ```bash
   frame new https://github.com/abogoyavlensky/clojure-stack-lite myproject
   ```

   To skip the prompts, pass answers with `--defaults --var` (see [Options](#options) for the keys):

   ```bash
   frame new --defaults \
     --var db=postgres,auth=true,daisyui=true,deploy=none,developer=yourname \
     https://github.com/abogoyavlensky/clojure-stack-lite myproject
   ```

2. Start development (with [mise](https://mise.jdx.dev/getting-started.html)):

   ```shell
   cd myproject
   mise trust && mise install
   bb clj-repl
   (reset)
   ```

   The server should be available at `http://localhost:8000`.
   Check out project's README.md and template documentation for more information on how to use the project.

> [!TIP]
> Edit some details before going to production: label in `Dockerfile`, 
  domain in `resources/public/manifest.json` and description in `README.md`

## Features

- 🏗️ Robust Clojure stack powered by Integrant and Reitit/Ring
- 🎨 Lightweight frontend using HTMX, AlpineJS and TailwindCSS v4 (with optional DaisyUI
  components)
- 📦 SQLite/PostgreSQL database (you choose)
- 🔄 Zero-downtime deployment via Kamal
- ⚡ GitHub Actions CI/CD pipeline
- 🧪 Integration and unit testing setup with coverage
- 🔍 Linting, formatting and deps version management
- ⚙️ deps.edn and Babashka Tasks for efficient project management
- 📱 Basic PWA support out of the box (without service worker)
- 🔒 Optional authentication and registration flow

## Stack

### Backend
- **Integrant**: Component lifecycle management for application
- **Reitit**: Fast data-driven routing
- **Ring/Jetty**: HTTP server adapter
- **Hiccup**: HTML generation from Clojure data structures
- **Malli**: Data validation and specification

### Database

- **SQLite/PostgreSQL**: Choose between file-based SQLite or enterprise-grade PostgreSQL
- **next.jdbc**: JDBC-based database access
- **HoneySQL**: SQL as Clojure data structures
- **Ragtime**: Database migrations

### Frontend
- **HTMX 2**: HTML extensions for AJAX without writing JavaScript
- **AlpineJS 3**: Lightweight JavaScript framework for adding behavior
- **TailwindCSS 4**: Utility-first CSS framework
- [OPTIONAL] **DaisyUI**: A UI Component library (`:daisyui true` option)

### Development
- **Babashka**: Project management with tasks
- **clj-kondo**: Static analyzer and linter
- **cljfmt**: Code formatter
- **eftest/cloverage**: Testing and code coverage

### Deployment
- **Docker**: Containerization
- **Kamal**: Zero-downtime deployments
- **GitHub Actions**: CI/CD workflows

### Features
- Authentication and registration flow (`:auth true` option)
  - Login, logout, registration, password reset, password change, account page 

## Project structure

The template generates a Clojure project with the following structure:

```
├── .clj-kondo/            # Clojure linting configuration
├── .github/               # GitHub Actions workflows and configurations
├── .kamal/                # Kamal deployment configuration (only used with Kamal)
├── db/                    # Empty database directory for database files (only used with SQLite)
├── dev/                   # Development configuration directory
│   └── user.clj           # User-specific development configuration
├── resources/             # Static resources and configuration files
│   ├── public/            # Public assets (CSS, JS, images)
│   ├── migrations/        # Database migration files
│   ├── config.edn         # Main configuration file for the application
│   ├── config.dev.edn     # Development-specific configuration
│   └── logback.xml        # Logging configuration file
├── src/                   # Source code directory
│   └── {{name}}           # Main namespace directory
│       ├── core.clj       # Application entry point
│       ├── db.clj         # Database system component and main operations
│       ├── handlers.clj   # HTTP request handlers
│       ├── routes.clj     # Route definitions
│       ├── server.clj     # Server system component
│       └── views.clj      # HTML templates and components with Hiccup
├── test/                  # Test files directory
│   └── {{name}}           # Test namespace directory
│       ├── home_test.clj  # Example test for home page
│       └── test_utils.clj # Test utilities
├── .cljfmt.edn            # Formatting configuration
├── .gitignore             # Git ignore rules
├── .mise.toml             # mise-en-place configuration with system dependencies
├── .dockerignore          # Docker ignore rules
├── bb.edn                 # Babashka tasks configuration for managing application
├── deps.edn               # Clojure dependencies and aliases
├── Dockerfile             # Dockerfile for building the application image
├── docker-compose.yaml    # Run PostgreSQL database for local development (only used with PostgreSQL)
├── LICENSE                # License file, AGPLv3 by default, for motivation check: https://plausible.io/blog/open-source-licenses
└── README.md              # Project documentation
```

## Options

The template offers customization options for generating your project:

- `:db` - Choose between SQLite or PostgreSQL for your database (*Default: `:sqlite`*)
    - Available values: `:sqlite` or `:postgres`
- `:daisyui` - Include [DaisyUI](https://daisyui.com/), a component library for TailwindCSS (*Default: `false`*)
    - Available values: `true`
- `:deploy` - Choose between Kamal and no deployment configuration (*Default:* `:kamal`)
    - Available values: `:kamal` or `:none`
- `:auth` - Add authentication and registration flow (*Default: `false`*)
    - Available values: `true`

Possible values: `false | true`

Usage example:

```shell
clojure -Tnew create :template io.github.abogoyavlensky/clojure-stack-lite :name myproject :daisyui true
```

## Authentication flow preview

https://github.com/user-attachments/assets/187daef7-d39d-4794-8dd4-2a88b735c826

## Roadmap

- [x] DaisyUI support
- [x] PostgreSQL support
- [x] No deployment option
- [x] Register/Auth flow
- [ ] Sentry support
- [ ] More frontend tool options (TwinSpark, Datastar)
- [ ] Queue support
- [ ] Websocket support

## Links

- [Integrant + Aero](https://lambdaisland.com/blog/2019-12-11-advent-of-parens-11-integrant-in-practice)
- [Start TailwindCSS as part of the app system](https://shagunagrawal.me/posts/multiplayer-board-game-in-clojure/#repl)
- [Auto-reloading Ring/Reitit](https://bogoyavlensky.com/blog/auto-reloading-ring/)
- [Sessions with Ring/Reitit](https://github.com/metosin/reitit/issues/205)
- [Clojure + Kamal](https://bogoyavlensky.com/blog/deploying-full-stack-clojure-app-with-kamal/)
- [CI with Kamal](https://igor.works/blog/evolution-of-github-action-for-kamal)

## Template Development

### Requirements

To work with this template, you need:

1. [mise](https://mise.jdx.dev/) (recommended) or manual installation of:
    - Java
    - Clojure
    - Babashka

### Getting Started

All management tasks:
```shell
bb tasks
The following tasks are available:

test    Run tests for the template config
new     Create a new project
release Create and push a new git tag based on provided version
```

After you updated the template and ran tests `bb test`, you can create a new project to check if everything works as expected:

```shell
bb new
```
The new project will be created in the `tmpl` directory at the root that is ignored by git.

### Release

Once you are ready to release a new version of the template, bump version in `deps.edn`:

```
:aliases -> :build -> :exec-args -> :version -> "0.1.1
```

and then run the following command:

```shell
bb release 
```

A new git tag based on latest version will be created and pushed to the repository.

## License
MIT License
Copyright (c) 2025 Andrey Bogoyavlenskiy

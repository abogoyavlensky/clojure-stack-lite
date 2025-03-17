# Clojure Stack Lite

A quick way to start a full-stack Clojure app with server-side rendering. 
Based on SQLite, HTMX, AlpineJS, and TailwindCSS v4.

## Usage

1. Create a new Clojure project using Clojure CLI:
   ```bash
   clojure -Ttools install-latest :lib io.github.seancorfield/deps-new :as new
   clojure -Sdeps '{:override-deps {org.clojure/clojure {:mvn/version "1.12.0"}}}' -Tnew create :template io.github.abogoyavlensky/clojure-stack-lite :name myproject
   ```

   Or alternatively using [neil](https://github.com/babashka/neil):

   ```bash
   brew install babashka/brew/neil
   neil new io.github.abogoyavlensky/clojure-stack-lite myproject
   ```

2. Start development:

   ```shell
   cd myproject
   bb repl 
   (reset)
   ```

   The server should be available at `http://localhost:8000`.
   Check out project's README.md and template documentation for more information on how to use the project.

> [!TIP]
> You might need to update the project configuration for going to production:
    - Edit image label in `Dockerfile`
    - Edit project domain in `resources/public/manifest.json`
    - Edit project description in `README.md`

## Features

- Server-Side Rendering with HTMX and Hiccup
- Dynamic Frontend with HTMX and AlpineJS without build step
- Utility-first CSS with TailwindCSS v4
- Lightweight and Fast: SQLite database
- Zero-downtime deployment with Kamal
- Integrated CI/CD with GitHub Actions
- Hot reloading for rapid development
- REPL-driven development support
- End-to-end testing setup with code coverage
- Dependency management with deps.edn
- Babashka tasks for common operations
- Linting and formatting tools setup

## Stack

### Backend
- **Integrant**: Component lifecycle management for application
- **Reitit**: Fast data-driven routing
- **Ring/Jetty**: HTTP server adapter
- **Hiccup**: HTML generation from Clojure data structures

### Database
- **SQLite**: Lightweight, file-based database
- **next.jdbc**: JDBC-based database access
- **HoneySQL**: SQL as Clojure data structures
- **Ragtime**: Database migrations

### Frontend
- **HTMX 2**: HTML extensions for AJAX without writing JavaScript
- **AlpineJS 3**: Lightweight JavaScript framework for adding behavior
- **TailwindCSS 4**: Utility-first CSS framework

### Development
- **Babashka**: Project management with tasks
- **clj-kondo**: Static analyzer and linter
- **cljfmt**: Code formatter
- **eftest/cloverage**: Testing and code coverage

### Deployment
- **Docker**: Containerization
- **Kamal**: Zero-downtime deployments
- **GitHub Actions**: CI/CD workflows

## Project structure

The template generates a Clojure project with the following structure:

```
├── .clj-kondo/            # Clojure linting configuration
├── .github/               # GitHub Actions workflows and configurations
├── .kamal/                # Kamal secrets template (only used if you use Kamal)
├── config/                # Kamal deployment configuration (only used if you use Kamal)
├── db/                    # Database empty directory (only used if you use SQLite)
├── dev/                   # Development configuration directory
│   └── user.clj           # User-specific development configuration
├── resources/             # Static resources and configuration files
│   ├── public/            # Public assets (CSS, JS, images)
│   ├── migrations/        # Database migration files
│   ├── config.edn         # Main configuration file for the application
│   ├── config.dev.edn     # Development-specific configuration
│   ├── config.e2e.edn     # Test-specific configuration for end-to-end testing
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
├── bb.edn                 # Babashka tasks configuration for managing application
├── deps.edn               # Clojure dependencies and aliases
├── Dockerfile             # Dockerfile for building the application image
├── LICENSE                # License file, AGPLv3 by default
└── README.md              # Project documentation
```

## Development

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

# Clojure Stack Lite

A quick way to start a full-stack Clojure app with server-side rendering.
Based on SQLite, HTMX, AlpineJS and TailwindCSS v4.

## Features

_TODO: add features!_

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

2. Update the project configuration:
    - Edit documentation in `README.md`

    TODO: add more instructions for updating the project configuration!

3. Start development:
   Manage project and start server from built-in REPL:
   ```shell
   cd myproject
   # Run server in dev mode
   bb repl 
   (reset)
   ```
   
   The server should be available at `http://localhost:8000`.
   Check out README.md for more information on how to use the project.

## Project structure

The template generates a Clojure project with the following structure:


```
├── .clj-kondo/            # Clojure linting cache, imports and configuration
│   └── config.edn         # Clojure linting configuration
├── .github/               # GitHub Actions workflows and configurations
│   ├── actions/           # Common actions for workflows
│   └── workflows/         # GitHub Actions workflow definitions
│       ├── checks.yaml    # Lint, format, test and check outdated deps on push
│       └── deploy.yaml    # Deployment workflow
├── .kamal/                # Kamal secrets template (_only used if you use Kamal_)
├── config/                # Kamal deployment configuration (_only used if you use Kamal_)
├── db/                    # Database files directory (_only used if you use SQLite_)
├── dev/                   # Development configuration directory
│   └── user.clj           # User-specific development configuration
├── resources/             # Static resources and configuration files
│   ├── public/            # Public assets (CSS, JS, images)
│   ├── migrations/        # Database migration files
│   ├── config.edn         # Main configuration file for the application
│   ├── config.dev.edn     # Development-specific configuration
│   ├── config.e2e.edn     # Test-specific configuration for end-to-end testing
│   └── logback.xml/       # Logging configuration file
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
│       ├── test_utils.clj # Test utilities
│       └── webdriver.clj  # Webdriver system component to be used in `config.e2e.edn`
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

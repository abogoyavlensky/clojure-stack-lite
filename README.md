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
   ```shell
   # Set up repository
   git init
   git add .
   # Run all checks
   bb check
   # Commit initial changes
   git commit -m "Initial commit"
   ```

   Manage project and start server from built-in REPL:
   ```shell
   # Show all available tasks
   bb tasks
   bb check
   bb repl 
   (reset)
   ```
   
   The server should be available at `http://localhost:8000`.
   Check out README.md for more information on how to use the project.

## Project structure

The template generates a Clojure project with the following structure:

; TODO: provide actual project structure!
```
├── .clj-kondo/            # Clojure linting configuration
├── .github/               # GitHub Actions workflows and configurations
├── dev/                   # Development configuration directory
│   └── user.clj           # User-specific development configuration
├── src/                   # Source code directory
│   └── {{name}}           # Main namespace directory
│       └── core.clj       # Main namespace file
├── test/                  # Test files directory
│   └── {{name}}           # Test namespace directory
│       └── core_test.clj  # Test namespace file
├── .cljfmt.edn            # Formatting configuration
├── .gitignore             # Git ignore rules
├── .mise.toml             # mise-en-place configuration with system tools versions
├── bb.edn                 # Babashka tasks configuration
├── deps.edn               # Clojure dependencies and aliases
├── LICENSE                # License file
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

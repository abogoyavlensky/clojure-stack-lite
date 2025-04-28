# {{main/ns}}

_This application is generated with [clojure-stack-lite](https://github.com/abogoyavlensky/clojure-stack-lite)._

_TODO: add project description_


## Development

Install Java, Clojure, Babashka, TailwindCSS and other tools manually or via [mise](https://mise.jdx.dev/):

```shell
mise trust && mise install
```

Check all available commands:

```shell
bb tasks
```

Run lint, formatting, tests and checking outdated dependencies:

```shell
bb check
```

Run server with built-in REPL from terminal:

> [!NOTE]
> If you're using PostgreSQL, [Docker](https://docs.docker.com/engine/install/) should be installed

 ```shell
bb clj-repl 
(reset)
````

Once server is started, it will automatically reload on code changes in the backend and TailwindCSS classes.
The server should be available at `http://localhost:8000`.

## Deploy from local machine using Kamal

Create `.env` file with variables: 
```bash
SERVER_IP=192.168.0.1
REGISTRY_USERNAME=your-github-username
REGISTRY_PASSWORD=personal-github-token
APP_DOMAIN=app.domain.com
SESSION_SECRET_KEY=secret-key
```

Add `ruby = "3.3.0"` to your local `.mise.toml` or to global `~/.mise/config.toml` file.

Install ruby and kamal:

```shell
brew install libyaml  # or for Ubuntu: `sudo apt-get install libyaml-dev` 
mise install ruby
gem install kamal -v 2.5.3
```

First deploy to the fresh server:

```shell
bb kamal setup
```

### Regular deployment

```shell
bb kamal deploy
```

## Deploy from Github Actions

Setup secrets for Actions:

```shell
SERVER_IP=192.168.0.1
APP_DOMAIN=app.domain.com
SSH_PRIVATE_KEY=secret-ssh-key
SESSION_SECRET_KEY=secret-key
```

### Notes

- `SSH_PRIVATE_KEY` - a new SSH private key **without password** that you created and added public part of it to the server's `~/.ssh/authorized_keys` to authorize from CI-worker.

To generate SSH keys, run:

```shell
ssh-keygen -t ed25519 -C "your_email@example.com"
```

## Update assets

The idea is to vendor all js-files in the project repo eliminating build step for js part.

Once you want to update the version of AlpineJS, HTMX or add a new asset, edit version in bb.edn file at `fetch-assets` and run:

```shell
bb fetch-assets
```

Your assets will be updated in `resources/public` folder.


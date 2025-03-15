# {{main/ns}}

_TODO: add project description_


## Development

Install [mise-en-place](https://mise.jdx.dev/getting-started.html#quickstart) (or [asdf](https://asdf-vm.com/guide/getting-started.html)),
then to install system deps run:

```shell
mise install
```

Check all available commands:

```shell
bb tasks 
```

_TODO: add futher instructions for development!_


## Deploy from local machine using Kamal

Create `.env` file with variables: 
```bash
SERVER_IP=192.168.0.1
REGISTRY_USERNAME=your-github-username
REGISTRY_PASSWORD=personal-github-token
APP_DOMAIN=app.domain.com
SESSION_SECRET_KEY=secret-key
```

Install ruby and kamal:

```shell
mise install ruby
gem install kamal -v 2.3.0
```

First deploy to the fresh server:

```shell
kamal setup
```

### Regular deployment

```shell
kamal deploy
```

## Deploy from Github Actions

Setup secrets fro Actions:

```shell
SERVER_IP=192.168.0.1
APP_DOMAIN=app.domain.com
SSH_PRIVATE_KEY=secret-ssh-key
```

### Notes

- `SSH_PRIVATE_KEY` - a new SSH private key **without password** that you created and added public part of it to the server's `~/.ssh/authorized_keys` to authorize from CI-worker.

To generate SSH keys, run:

```shell
ssh-keygen -t ed25519 -C "your_email@example.com"
```

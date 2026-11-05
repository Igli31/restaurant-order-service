# Publishing order-service to GHCR

The `.github/workflows/ghcr.yml` workflow runs when a commit is pushed to
`development`, including a locally merged feature branch. Pushing a feature
branch alone does not publish an image.

It checks out the code, logs in to GHCR with GitHub's automatic `GITHUB_TOKEN`,
builds the Dockerfile (which runs Maven tests), and publishes only if the build
succeeds. The job needs `packages: write`; no personal access token or password
needs to be added as a repository secret.

The image name is derived from the repository and converted to lowercase:

```text
ghcr.io/igli31/restaurant-order-service:development
ghcr.io/igli31/restaurant-order-service:sha-<full-commit-sha>
```

`development` moves to the latest published development build. The SHA tag
identifies the exact source commit. Later, Helm can select a specific image tag
for a Kubernetes deployment. Publishing an image does not deploy it.

This workflow only publishes development images for now. Testing and release
promotion will be configured when those stages are introduced.

After pushing to development, open GitHub Actions and inspect **Publish order
image → Build and publish**. After success, look for the image in your GitHub
profile's Packages section or the repository's linked packages. New GHCR
packages are private by default. Pulling a private image locally requires
separate registry authentication; we will configure Kubernetes access later.

If publishing returns a permissions error, check the repository/organization's
Actions policies. An already existing package may also need to grant this
repository Actions access.

References: [GitHub package publishing](https://docs.github.com/en/packages/managing-github-packages-using-github-actions-workflows/publishing-and-installing-a-package-with-github-actions),
[Docker GHCR login action](https://github.com/docker/login-action#github-container-registry).

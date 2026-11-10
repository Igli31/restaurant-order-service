# Automatic order-service development and testing deployment

## Sequence

1. Work on a feature branch created from main; commit and push it.
2. Check out the target branch (development or testing), pull, merge this same
   feature branch directly into it, then push that target branch.
3. GitHub runs the existing CI workflow and the GHCR workflow.
4. The GHCR job builds/tests the Dockerfile and publishes the commit's SHA tag.
5. Only after that job succeeds, it calls `.github/workflows/cd.yml`.
6. CD checks out that exact commit on your Windows self-hosted runner and runs
   Helm against Docker Desktop Kubernetes's matching namespace.
7. CD verifies the rollout and that the expected image is selected.

The separate CI workflow also runs, but CD is gated by the publishing job's
success. That publishing job itself runs the Maven tests through the Dockerfile.
Neither feature pushes nor pull requests run CD. Production CD comes later.

## Why two workflow files change

`cd.yml` is a reusable workflow (`workflow_call`), not an independent push trigger.
`ghcr.yml` calls it through `needs: publish`, so an image cannot be deployed before
it is published successfully. The modified GHCR workflow is included on this
feature branch because it is part of this change; unchanged Docker/Helm files
remain on development until the merge.

A local reusable workflow is loaded from the same commit as its caller. Unlike
a workflow_run trigger, this approach does not require the CD file to exist on
the repository's default main branch first.

## Runner prerequisites

- The repository's runner `restaurant-order-local` is running as your Windows
  user. Labels `self-hosted`, `Windows`, and `X64` are enough; the skipped extra
  label is not required.
- Docker Desktop and its Kubernetes cluster are running.
- Helm 4, kubectl, and Git are available on the runner's PATH. Restart the runner
  after changing its environment variables or installing missing tools.
- Your Windows user's kubeconfig includes the `docker-desktop` context.
- Both target namespaces exist, with a valid `ghcr-pull` Secret in each.
- Each target Git branch must contain the Dockerfile, chart, and environment
  values before pushing. Testing still needs its prerequisite feature merges.

No kubeconfig or registry token is committed. The runner uses your local cluster
credentials; Kubernetes uses its existing registry Secret to download the image.
All cluster commands explicitly select docker-desktop to avoid depending on a
terminal's currently selected context.

Keep run.cmd open for now. If the runner is offline, GitHub queues the deployment;
already deployed Pods continue running while Docker Desktop runs. Build/test and
publishing stay on GitHub-hosted Ubuntu runners. Do not route untrusted PR jobs to
this laptop runner. These deployment jobs run trusted development or testing pushes only.

## Image and chart versions

Helm receives `--set-string image.tag=sha-<commit>` for the image just published.
This overrides the earlier manually pinned image in the values file for this
installation; it does not rewrite or commit values.yaml. Environment settings
come from values-development.yaml or values-testing.yaml according to the branch. Chart.yaml's chart version should be
incremented when chart configuration changes; no Git tag is required to trigger
this push-based deployment.

The H2 chart keeps one replica, Recreate updates, and its existing volume. Updates
briefly interrupt service while replacing the Pod. Helm 4's rollback-on-failure
restores a previous release after an unsuccessful upgrade; it does not undo
database changes or restore old data.

## Verify after the first merge and push

In GitHub Actions, open Publish order image. After Build and publish succeeds,
Deploy development or Deploy testing runs on the laptop. The final step prints the expected SHA
image. Then inspect from your own terminal:

```powershell
helm list --kube-context docker-desktop --namespace development
kubectl --context docker-desktop --namespace development get pods
kubectl --context docker-desktop --namespace development get deployment restaurant-order -o 'jsonpath={.spec.template.spec.containers[0].image}'
```

If a deployment waits indefinitely for a runner, check that run.cmd says Listening
for Jobs and that the GitHub repository shows its Windows runner as online.
If access/tools fail, inspect the failed job's Check runner tools step; keep tokens
and kubeconfig contents private. Helm's wait/probes verify readiness; order
placement still needs the menu-service and is checked through the frontend.

References: [Reusable workflows](https://docs.github.com/en/actions/reference/workflows-and-actions/reusing-workflow-configurations),
[Helm upgrade](https://helm.sh/docs/helm/helm_upgrade/).

## Environment selection and first testing deployment

Pushes to development publish the development image alias and deploy into the
development namespace. Pushes to testing publish the testing alias and deploy
into testing. Both also publish sha-<commit>; Helm deploys that exact SHA tag.
The namespace and values file come from the branch name. No release tag is needed.

Continue using this existing feature/cd branch: commit and push it, merge into
development, verify it, then merge the same feature directly into testing. Do not
merge development into testing. Testing must first receive the CI, Docker, GHCR,
and Helm features too; stop on any merge conflict and resolve it before pushing.

Each namespace has separate Pods, Services, and H2 database storage. The existing
order runner can deploy to either namespace. Feign reaches restaurant-menu in
that same namespace; testing order placement needs testing menu-service deployed.

A push to testing builds its merged commit rather than automatically promoting
the identical binary digest previously built from development. This is our chosen
branch-push pipeline, distinct from a build-once artifact promotion pipeline.

Inspect testing after deployment:

```powershell
kubectl --context docker-desktop get pods,svc,pvc -n testing
helm list --kube-context docker-desktop -n testing
kubectl --context docker-desktop -n testing get deployment restaurant-order -o 'jsonpath={.spec.template.spec.containers[0].image}'
```

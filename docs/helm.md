# Order-service Helm chart

Chart directory: `helm/restaurant-order`.

## Files and versions

- `Chart.yaml`: chart metadata. `version: 0.1.0` is the chart's version; increment
  it when changing the chart. `appVersion` describes the application's Maven
  version and does not automatically select a container image.
- `values.yaml`: shared defaults, including image, registry Secret, and storage.
- `values-development.yaml` and `values-testing.yaml`: environment overrides.
- `templates/`: Kubernetes Deployment, Service, and database volume claim.
- `image.tag`: selects the existing GHCR image to run, here the full commit SHA
  from the successful development publish. Promoting an image does not rebuild it.

Install the same release name in different namespaces for separate environments.
Each namespace gets its own Pod, Service, and H2 database. Docker named volumes
from earlier are not reused; Kubernetes starts a separate database.

The deployment deliberately uses one replica and Recreate updates because H2
is a file database. Do not scale it to multiple replicas sharing that file.
Kubernetes' fsGroup gives the application access to the mounted database volume.
The volume claim is retained when the Helm release is uninstalled.
This does not protect data from a cluster reset or explicit storage deletion.

## Prerequisites

- Docker Desktop Kubernetes context: `docker-desktop`.
- Namespaces `development` and `testing` already created.
- A registry Secret named `ghcr-pull` in each namespace you deploy to, authorized
  to read the private GHCR package. Never commit registry credentials or paste
  them into chat. We will create this separately before the first installation.
- The default Kubernetes StorageClass, used for the 1Gi volume claim.
- For placing orders, a Kubernetes menu-service Service named `restaurant-menu`
  on port 8081 in the same namespace. A Docker container with that name is not
  that Kubernetes Service. Its chart is the next step.

## Validate before deployment

Run from the order-service project directory:

```powershell
helm lint ./helm/restaurant-order --strict -f ./helm/restaurant-order/values-development.yaml
helm template restaurant-order ./helm/restaurant-order --namespace development -f ./helm/restaurant-order/values-development.yaml
helm lint ./helm/restaurant-order --strict -f ./helm/restaurant-order/values-testing.yaml
```

These commands inspect/render the chart; they do not install it.

## Install development (after registry authentication is ready)

```powershell
helm upgrade --install restaurant-order ./helm/restaurant-order --namespace development -f ./helm/restaurant-order/values-development.yaml --wait --timeout 5m
kubectl get pods,svc,pvc -n development
kubectl logs -n development deployment/restaurant-order
```

To reach it from your browser without colliding with the Docker container's 8082:

```powershell
kubectl port-forward -n development service/restaurant-order 18082:8082
```

Keep that terminal open and visit http://localhost:18082/api/orders.
Startup/liveness probes check the listening port. Readiness checks the order API.
Readiness does not guarantee that menu-service is available; order placement
still depends on Feign reaching it.

## Testing later

After promoting the feature branches directly into `testing` according to our
Git workflow and selecting the tested image tag:

```powershell
helm upgrade --install restaurant-order ./helm/restaurant-order --namespace testing -f ./helm/restaurant-order/values-testing.yaml --wait --timeout 5m
```

We will add automated deployment (CD) after manual deployment works.

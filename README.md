# CWM-Wings

Minecraft Plugin for custom wings.

## CI/CD and Releases

This project uses GitHub Actions for continuous integration and automated releases.

### Workflows

- **Build**: Triggered on every push and pull request to `main`, `master`, or `develop`.
  - Compiles the project using JDK 21.
  - Runs unit tests.
  - Performs compatibility tests on Paper servers (versions 1.20.4, 1.21.1, 1.21.4).
  - Uploads the built JAR as a GitHub artifact.
- **Release**: Triggered when a tag starting with `v` is pushed (e.g., `v1.0.0`).
  - Builds the shadow JAR.
  - Automatically generates a changelog from commits.
  - Creates a GitHub Release and attaches the JAR.
  - Automatically marks the release as **pre-release** if the tag contains `alpha`, `beta`, or `rc`.

### How to publish a release

1. Ensure all changes are committed and pushed to the main branch.
2. Create a new semantic version tag:
   ```bash
   git tag -a v1.0.0 -m "Release v1.0.0"
   ```
   *For a pre-release:*
   ```bash
   git tag -a v1.0.0-beta.1 -m "Release v1.0.0-beta.1"
   ```
3. Push the tag to GitHub:
   ```bash
   git push origin v1.0.0
   ```
4. The GitHub Action will automatically handle the rest.

## Compatibility Note (Minecraft 1.8)

Currently, the plugin target is **Minecraft 1.21.4** and requires **Java 21**. 
Full support for Minecraft 1.8 is not possible with the current architecture due to:
1. **Java Version**: Minecraft 1.8 runs on Java 8, while this plugin uses Java 21 features.
2. **API Changes**: Many Paper/Bukkit API features used (like `NamespacedKey`, `itemModel`, `EquipmentSlot`) were introduced in later versions.

**Recommended strategy for 1.8 support:**
If 1.8 support is required, the project should be refactored into a **multi-module** project:
- `cwm-wings-api`: Common interfaces and data models.
- `cwm-wings-v1_8`: Implementation for 1.8 using NMS or legacy Bukkit API (Java 8).
- `cwm-wings-v1_21`: Current implementation (Java 21).
- `cwm-wings-plugin`: Main entry point that detects server version and loads the appropriate module.

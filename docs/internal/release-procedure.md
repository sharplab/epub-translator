# Release procedure

This document describes epub-translator release procedure.

### Release execution

The release is performed using GitHub Actions.

1.  Navigate to the **Actions** tab in the GitHub repository.
2.  Select the **Release** workflow from the sidebar.
3.  Click the **Run workflow** dropdown and select the branch (usually `master`).
4.  Click the **Run workflow** button.

This workflow will:
- Update the version in `gradle.properties` (remove `-SNAPSHOT` and add `.RELEASE`).
- Build the uber-jar.
- Commit and push the release change.
- Create and push a Git tag (e.g., `0.7.0.RELEASE`).
- Create a GitHub Release and upload the `epub-translator-runner.jar`.

### Post-release (Automatic)

After the **Release** workflow completes successfully, the **Bump version** workflow will automatically start.

This workflow will:
- Increment the patch version in `gradle.properties`.
- Switch back to `SNAPSHOT` mode.
- Commit and push the change to the repository.

You can also trigger the **Bump version** workflow manually if needed.

### Manual update of release notes

Although the release is created automatically with generated notes, you may want to refine them:

1.  Go to the **Releases** page on GitHub.
2.  Edit the newly created release.
3.  Adjust the release notes as necessary and save.
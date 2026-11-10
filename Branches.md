# EcoScan — Branches Guide

## 1. Branch Structure

Each part of EcoScan has its own development branch.

For example:

`feature/results-recommendation-component`

**Important rules:**

- Work on your own feature branch, not directly on the shared development branch(main).
- Keep each feature branch focused on one task.
- Merge completed features into main through a GitHub pull request.
- Do not merge unfinished feature branches into one another unless agreed with the team.
- If your feature depends on another feature, wait until the required feature has been merged into the parent branch before incorporating it.
- The existing current branches are a guide, if a branch doesn't exist then you're totally fine with making it yourself. Just try and follow the above naming pattern

---

## 2. Creating a New Feature Branch

Before starting a new task, retrieve the latest remote branch information:

```bash
git fetch origin
```

Create your feature branch from the appropriate parent branch:

```bash
git switch -c feature/history-delete-scans origin/main
```

This ensures your new branch starts with the latest version of `main`.

When you're ready to publish the branch:

```bash
git push -u origin feature/history-delete-scans
```

After this first push, you can normally use `git push` without specifying the branch name.

---

## 3. Updating an Existing Feature Branch

Sometimes another teammate completes functionality that your feature needs.

For example:

- You're working on deleting scans from the History page.
- Another teammate is implementing CSV scan imports.
- Your delete-scans functionality depends on the imported scan data.

The correct approach is to finish and merge the import functionality first, then update your delete-scans branch.

### Step 1 — Complete the required feature

The developer working on CSV imports completes and tests their changes.

They push their branch:

```bash
git switch feature/history-import-scans
git push origin feature/history-import-scans
```

They then create a GitHub pull request:

- **Base:** `main`
- **Compare:** `feature/history-import-scans`

[GitHub guide to creating pull requests](https://docs.github.com/en/pull-requests/how-tos/create-pull-requests/creating-a-pull-request)

The pull request should be reviewed and merged before dependent work is integrated.

### Step 2 — Confirm the dependency has been merged

Before updating your own branch:

1. Check Trello to confirm the required task is complete.
2. Check GitHub to confirm its pull request has been merged.
3. If anything is unclear, ask in Discord.

**Don't assume a feature is available just because someone says they've finished coding it.**

### Step 3 — Switch to your feature branch

Check your current branch:

```bash
git branch --show-current
```

If you're on the wrong branch:

```bash
git switch feature/history-delete-scans
```

### Step 4 — Check for uncommitted changes

Run:

```bash
git status
```

Ideally, Git should report:

`nothing to commit, working tree clean`

If you have uncommitted changes, commit or stash them before proceeding.

Only discard changes if you're certain they are unnecessary.

### Step 5 — Fetch the latest remote changes

Run:

```bash
git fetch origin
```

This retrieves the latest information about remote branches without modifying your current files.

### Step 6 — Merge the updated parent branch

While still on `feature/history-delete-scans`, run:

```bash
git merge origin/main
```

This brings the latest completed functionality, including CSV imports, into your feature branch.

It does not merge your unfinished delete-scans functionality into `main`.

**If a merge conflict occurs:**

- Read the conflicting changes carefully.
- Don't automatically accept the newer version.
- Preserve both changes where appropriate.
- Ask the teammate responsible for the affected code if you're unsure.
- Finish resolving the conflict before continuing.

### Step 7 — Test and push

Once the merge completes:

- Confirm EcoScan still compiles.
- Test the functionality affected by the merge.
- Continue developing your feature.

Push your updated branch:

```bash
git push origin feature/history-delete-scans
```

When your feature is finished and tested, create a pull request:

- **Base:** `main`
- **Compare:** `feature/history-delete-scans`

---

## 4. Before Opening a Pull Request

Make sure:

- [ ] Your feature is complete.
- [ ] The project compiles successfully.
- [ ] Your changes work as expected.
- [ ] You haven't accidentally included unrelated changes.
- [ ] Your branch includes any required completed work another branch
- [ ] All changes are committed and pushed.

Your pull request should briefly explain what you changed and how you tested it.

---

## 5. Branch Naming Conventions

| Prefix | Purpose | Example |
|---|---|---|
| `feature/` | New functionality | `feature/history-import-scans` |
| `design/` | Visual or styling changes | `design/results-layout` |
| `bug/` | Fixing existing problems | `bug/history-loading-error` |
| `tweak/` | Improving existing functionality | `tweak/results-power-display` |

Use descriptive names that make the purpose of each branch obvious.

---

## 6. Common Git Commands

| Command | Purpose |
|---|---|
| `git status` | Check changes and working tree |
| `git branch --show-current` | Show your current branch |
| `git fetch origin` | Retrieve remote updates |
| `git switch branch-name` | Switch branches |
| `git merge origin/branch-name` | Merge remote branch changes into your current branch |
| `git add .` | Stage changes under the current directory |
| `git commit -m "message"` | Save staged changes locally |
| `git push` | Upload commits to the remote branch |

---

## 7. Final Reminders

**Keep branches small and focused.** One feature per branch makes reviewing and merging significantly easier.

**Don't work directly on shared development branches.** Use feature branches and pull requests.

**Communicate dependencies early.** If your task requires another teammate's work, coordinate through Trello or Discord.

**Ask before resolving unfamiliar conflicts.** It's better to spend a few minutes checking than accidentally remove someone else's work.

The aim is to keep everyone's changes organised and make integration as straightforward as possible.

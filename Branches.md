# Branches Guide

| Branch | Created From | Merges Into |
|--------|--------------|-------------|
|    feature/home-*    |       views-home       |      views-home       |
|    feature/loading-*    |       views-loading       |      views-loading       |
|    feature/results-*    |       views-results       |      views-results       |
|    feature/history-*    |       views-history       |      views-history       |


> e.g `feature/results-recommendation-component`  comes from `views-results` and will merge into `views-results` when ready

## Updating Branches
 ### Updating feature branches: 
 When a feature requires functionality that has been merged into its parent development branch,
 developers should fetch the latest remote changes and merge the parent branch into their feature branch. 


 
 
**Do not merge unfinished feature branches directly into one another unless agreed with the team.**
**Also, if you need something done in order to do your work then make sure its done in the correct order and separate. Don't combine multiple branches into 1
Example (actual branch names are probably different)
- You need to be able to import a .csv into the history page before you can delete scans from the history page
- You would complete import-csv branch first then merge that into views-history
- Then, while in the delete-scans branch you pull the latest views-history and work from that.
- Don't do it the other way around, that will just get really confusing for the person merging the code

## An example guide just to emphasise this using the import-scans and delete-scans branches
 
 ### Step 1 — finish and push import-scans functionality
 
push your work to the repo:

- ` git switch feature/history-import-scans `
- ` git push origin feature/history-import-scans `

You then create a GitHub pull request [Guide to Creating a Pull Request](https://docs.github.com/en/pull-requests/how-tos/create-pull-requests/creating-a-pull-request)
- ` Base:     views-history `
- ` Compare:  feature/history-import-scans `

### Step 2 - Once reviewed and merged, the import-scans functionality becomes part of views-history.

- You might just need to wait on the person reviewing your code.
- It's probably going to be Chris, so go easy on him there's only so many hours in the day.
- I'm doing this to avoid people have to worry about conflicts.

### Step 3 - You're working on the delete-scans function that needs the import-scans function
3.0 - Check that required work has been completed
- First check the trello
- then if its not clear ask in Discord
- *Don't assume anything has been done*

3.1 Check the current branch

> `git branch --show-current`

This should result in something like ` feature/history-delete-scans `
> you should know what your current active branch is but it's important to double check

3.1.1 If you're on the wrong branch then:

> ` git switch name-of-the-correct-branch ` 

3.2 Check for uncommitted changes ( you should have 0, otherwise commit them or undo them and proceed )

> ` git status `


3.3 Next, get the latest version of the project (this downloads info about all remote branches, including views-history)

> ` git fetch origin `


3.4 Merge the updated parent branch (with the import-scans code)
- This incorporates the latest views-history changes into feature/history-view-scan
- If there are conflicting changes, Git might ask you to resolve them before completing the merge.
- In this instance because you want the new code you should favour the new code (that will make sense when it happens)

> `git merge origin/views-history`

3.5 You should now be able to work on the delete-scans function with the import-scans function included in views-history

3.6 You can then push your work to github and continue working on the delete-scans branch.

> ` git push origin feature/history-view-scan ` 

End: Then once you're happy that the work is done you create a pull request like above






## Explanation
- feature/ - A new piece of code that involves functionality
- design/ - Only changes appearance 
- bug/ - bug
- tweak/ - A change to existing functionality


### Note
- I'm putting this together pretty quickly so i'll come back to it and more information
- Hopefully enough to give you a basic idea of what's going on with the branches

## Commit Message Format

This project utilizes [the Angular Conventional Changelog commit message format](https://github.com/conventional-changelog/conventional-changelog/tree/master/packages/conventional-changelog-angular#commit-message-format).
We use this format to automatically generate our Changelog file.

A commit message consists of a **header**, **body** and **footer**.  The header
has a **type**, **scope** and **subject**:

```
<type>(<scope>): <subject>
<BLANK LINE>
<body>
<BLANK LINE>
<footer>
```

The **header** is mandatory and the **scope** of the header is optional.

### Revert

If the commit reverts a previous commit, it should begin with `revert: `,
followed by the header of the reverted commit. In the body it should say:
`This reverts commit <hash>.`, where the hash is the SHA of the commit being
reverted.

### Type

If the prefix is `feat`, `fix` or `perf`, it will appear in the changelog.
However if there is any [BREAKING CHANGE](#footer), the commit will always
appear in the changelog.

Other prefixes are up to your discretion. Suggested prefixes are `build`, `ci`,
`docs` ,`style`, `refactor`, and `test` for non-changelog related tasks.

Details regarding these types can be found in the official [Angular Contributing
Guidelines](https://github.com/angular/angular/blob/master/CONTRIBUTING.md#type).

### Scope

The scope could be anything specifying place of the commit change. For example
`android`, `ios`, `ts`, `events`, etc...

### Subject

The subject contains succinct description of the change:

* use the imperative, present tense: "change" not "changed" nor "changes"
* don't capitalize first letter
* no dot (.) at the end

### Body

Just as in the **subject**, use the imperative, present tense: "change" not
"changed" nor "changes". The body should include the motivation for the change
and contrast this with previous behavior.

### Footer

The footer should contain any information about **Breaking Changes** and is also
the place to reference GitHub issues that this commit **Closes**.

**Breaking Changes** should start with the word `BREAKING CHANGE:` with a space
or two newlines. The rest of the commit message is then used for this.

A detailed explanation can be found in this [document](#commit-message-format).

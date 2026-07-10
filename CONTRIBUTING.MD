# Contribute to Dragon Launcher

## Issues:

- Read previous, check for no duplicate
- Try restarting app; clearing cache/data update to latest release/beta

## Pull Requests:

- If you plan to make big modifications, ask me before, to avoid wasting time
- Document code

## My values

- Always let the user the choice: everything should be customizable, that's why I have a kinda
  robust settings stores system aligned with the backups
- Never ANY internet access
- Entirely open source

## Code styles

- Use camelCase for variables and functions name, and globally everywhere
```kotlin
class AppsViewModel
```

- call Context instances `ctx`, not `context`
```kotlin
fun foo(ctx: Context) {
    return ctx.someFunction()
}
```

- Use snake_case for resources like strings or images
- Always add spaces between brackets in function declaration, condition statement

```kotlin
fun foo() {
    if (cond) {
        bar()
    }
}
```

- Put else statements in the same line as the closing if:

```kotlin
if (cond) {
    doSomething()
} else {
    doSomethingElse()
}
```

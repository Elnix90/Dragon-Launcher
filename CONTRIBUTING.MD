# Contribute to Dragon Launcher

## AI Usage
I DO NOT accept fully AI-made pull requests.
- You can use AI to *help* (not do all) you coding, but you have to understand 100% of the code you write.
- It's in my entire discretion to close your PR without further advice if I consider your PR AI-made.
- DO NOT WRITE YOUR COMMIT MESSAGES NOR ISSUES OR PULL REQUEST USING AI. Write them entirely by yourself, I, a human reads them and I don't want to read AI bullshit you didn't even bother verify
- I am the only one that determines what is AI or not in my repo and I do whatever I want.

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

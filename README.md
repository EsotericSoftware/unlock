
## Unlock

This utility for Windows uses `handle` from [Sysinternals](https://learn.microsoft.com/en-us/sysinternals/downloads/handle) to unlock a file. All open handles to the file by all processes are closed. This may cause the processes to malfunction, but sometimes freeing up the damn file is more important.

Usage:
```
java -jar unlock.jar [--list|-l] [path]
```

### handle

`handle` is difficult to use directly because it lists the handles and expects you to assemble further commands to do the unlocking, which is tedious. Also `handle` uses substring matching, eg `abc` will match both `abc` and `abcd`. Checking for an exact match via shell scripts is cryptic and difficult to get right.

If substring matching is sufficient, `handle` can be used with Cygwin like this:
```
handle -nobanner -v "$1" | sed '1d' | cut -f2,4 -d, | tr ',' '\n' | xargs -r sh -c "handle -nobanner -p $1 -c $2 -y" sh
```

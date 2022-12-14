
## Unlock

This utility for Windows uses `handle` from [Sysinternals](https://learn.microsoft.com/en-us/sysinternals/downloads/handle) to unlock a file. All open handles to the file by all processes are closed. This may cause the processes to malfunction, but sometimes freeing up the damn file is more important.

Usage:
```
java -jar unlock.jar [--list|-l] [path]
```

### Context menu

To add an `Unlock` entry to the context menu for all files and folders, place the following in a `.reg` file, modify the paths for your Java and `unlock.jar` files, and merge it into your registry.

```
Windows Registry Editor Version 5.00

[HKEY_CLASSES_ROOT\*\shell\Unlock]
@="&Unlock"

[HKEY_CLASSES_ROOT\*\shell\Unlock\Command]
@="\"C:\\path\\to\\java.exe\" -jar \"C:\\path\\to\\unlock.jar\" \"%1\""
```

### `handle`

`handle` is difficult to use directly because it lists the handles and expects you to assemble further commands to do the unlocking, which is tedious. Also `handle` uses substring matching, eg `abc` will match both `abc` and `abcd`. Checking for an exact match via shell scripts is cryptic and difficult to get right.

If substring matching is sufficient, `handle` can be used with Cygwin like this:
```
handle -nobanner -v "$1" | sed '1d' | cut -f2,4 -d, | tr ',' '\n' | xargs -r sh -c "handle -nobanner -p $1 -c $2 -y" sh
```

## Jadx scripting support with Kotlin

Jadx scripting guide can be found at [wiki page](https://github.com/skylot/jadx/wiki/Jadx-scripts-guide)

### Examples

Check script examples in [`examples/`](https://github.com/jadx-decompiler/jadx-script-kotlin/tree/main/examples)(start with [`hello`](https://github.com/jadx-decompiler/jadx-script-kotlin/tree/main/examples/hello.jadx.kts))


### How to install plugin
- jadx-cli:
  ```
  jadx plugins --install "github:jadx-decompiler:jadx-script-kotlin"
  ```
- jadx-gui:
  - in `File` menu select `Preferences`
  - go to `Plugins` section
  - select `Jadx Script (Kotlin)` from `Available` list
  - click `Install` button

### Scripts usage

#### In jadx-cli

Just add script file as input

#### In jadx-gui

1. Add script file to the project (using `Add files` or `New script` by right-click menu on `Inputs/Scripts`)
2. Script will appear in `Inputs/Scripts` section
3. After script change, you can run it using `Run` button in script editor toolbar or reload whole project (`Reload` button in toolbar or `F5`).
   Also, you can enable `Live reload` option in `File` menu to reload project automatically on scripts change

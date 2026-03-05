/**
* For script usage check plugin project at https://github.com/jadx-decompiler/jadx-script-kotlin
*/

val jadx = getJadxInstance()

jadx.afterLoad {
	log.info { "Hello from jadx script!" }
}

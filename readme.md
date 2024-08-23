# cljextract

cljextract is a thin wrapper around [jextract](https://github.com/openjdk/jextract/), offering an alternative way to build and invoke it in a way that might be more familiar for clojure users.

I made cljextract because I wanted the latest jextract features for which there wasn't yet a binary release, and I was struggling to build jextract on my system with their gradle script.

Notably, cljextract uses the [bytedeco](https://bytedeco.org/) Maven dependency and handles making it available to the native library loader, instead of requiring a specific (old version) of Clang to be installed.

I've only tried out cljextract on Linux. I have made a probably futile effort to keep it working cross platform. If you try out cljextract on other platforms and have issues, let me know. PRs welcome.


# Installation

First, add a cljextract alias to your system wide deps.edn (e.g. the one at `~/.clojure/deps.edn`)

```
; {:aliases { ...

:cljextract
{:extra-deps {io.github.jjttjj/cljextract {:git/sha "f0614d88ba725395ef9377e2145b4fe750068841"}}
            :main-opts  ["-m" "dev.jt.cljextract"]}
```

Then you will need to prep the library. This fetches the jextract souce code and caches and compiles it:
```
clojure -T:deps prep :aliases [:cljextract]
```

Note that we depend on the the llvm clang library which is ~1.7GB. This may take a long time to download on the first run, and there are some [bugs somewhere relating to these large downloads](https://ask.clojure.org/index.php/12730/error-could-acquire-write-lock-artifact-org-bytedeco-opencv?show=12730#q12730). If you have adding `-Sthreads 1` should work:
```
clojure -Sthreads 1 -T:deps prep :aliases [:cljextract]
```

Then you can invoke jextract with:
```
clojure -M:cljextract --help
```

The commands passed are exactly the same as jextract's. See [their guide](https://github.com/openjdk/jextract/blob/master/doc/GUIDE.md) for instructions.

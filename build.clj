(ns build
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.build.api :as b]
            [clojure.tools.gitlibs :as gitlibs]))

(def jextract-sha "e55aacfda23211406552357095b6eb780f0fa8e5")

(def jextract-root
  (delay
    (gitlibs/procure "https://github.com/openjdk/jextract"
      'org.openjdk/jextract
      jextract-sha)))

(def class-dir "target/classes")
(def jar-file "target/lib/jextract.jar")

(defn clean [& {:as opts}]
  (b/delete {:path "target"})
  opts)

(defn compile-jextract [& {:as opts}]
  (println "compiling java...")
  (b/javac
    {:src-dirs   [(str (io/file @jextract-root "src/main/java"))]
     :class-dir  class-dir
     :javac-opts ["--enable-preview"
                  "--source=22"]})
  (b/copy-dir
    {:src-dirs   [(str (io/file @jextract-root "src/main/resources"))
                  class-dir]
     :target-dir "target/classes"})
  opts)


(defn jextract-jar [& {:as opts}]
  (println "extracting jar...")
  (compile-jextract)
  (b/jar {:class-dir class-dir
          :jar-file jar-file}))


(comment
  (clean)
  (jextract-jar)
  )

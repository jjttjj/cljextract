(ns dev.jt.cljextract
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.java.shell :as shell])
  (:import [java.io File]
           [java.nio.file Files Paths Path]
           [java.nio.file.attribute FileAttribute]
           [org.bytedeco.javacpp Loader]))

(defn get-lib-dir []
    (let [platform-props (Loader/loadProperties)
          clazz          org.bytedeco.llvm.presets.clang
          lib-props      (Loader/loadProperties clazz platform-props true)
          libstr         "clang:@13"
          url            (first (Loader/findLibrary clazz lib-props libstr false))
          resource-dir   (Loader/cacheResource ^java.net.URL url)
          ^Path symlink-dir
          (Files/createTempDirectory
            (str "cljextract-clang-" (random-uuid))
            (make-array java.nio.file.attribute.FileAttribute 0))]
      (.deleteOnExit (.toFile symlink-dir))
      (doseq [^File f (.listFiles resource-dir)]
        (Files/createSymbolicLink
          (Paths/get (str symlink-dir) (into-array String [(.getName f)]))
          (.toPath f)
          (make-array FileAttribute 0)))
      (doseq [^File f (.listFiles (.toFile symlink-dir))]
        (Loader/createLibraryLink (.getAbsolutePath f)
          lib-props libstr (into-array String [])))
      symlink-dir))

(defn get-library-path-env-name []
  (case (System/getProperty "os.name")
    "Windows"  "PATH"
    "Mac OS X" "DYLD_LIBRARY_PATH"
    "Linux"    "LD_LIBRARY_PATH"
    :else
    (throw (UnsupportedOperationException. "Unsupported operating system"))))

(defn jextract [{:keys [dir jextract-args] :as opts}]
  (let [lib-var  (get-library-path-env-name)
        lib-path (str (some-> (System/getenv lib-var)
                              (str File/pathSeparator))
                      (get-lib-dir))
        args
        (concat
          (cond->> jextract-args (string? jextract-args) (str/split #"\s+"))
          (into [:env {lib-var lib-path}] cat (dissoc opts :jextract-args)))]
    (-> (apply shell/sh "java"
          "-cp" (-> (io/resource "jextract.jar") .toURI Paths/get str)
          "org.openjdk.jextract.JextractTool"
          args)
        ((some-fn :err :out))
        println)))

(defn -main [& args]
  (jextract {:jextract-args args})
  (shutdown-agents)
  (System/exit 0))

(comment
  (jextract {:jextract-args ["--help"]})
  )

(defproject elaveted-lein "2025.98.101-SNAPSHOT"
  :javac-options ["-target"
                  "17"
                  "-source"
                  "17"
                  "-g"
                  "-Xlint:-deprecation"
                  ]
  :dependencies
  [[org.clojure/clojure "1.12.4"]

   ;[org.clojure/spec.alpha "0.5.238"]
   ;[org.clojure/core.specs.alpha "0.4.74"]

   ;[io.earcam.instrumental/io.earcam.instrumental.compile "0.1.0"]
   ;[io.earcam.instrumental/io.earcam.instrumental.proxy "0.1.0"]
   ;[io.earcam.instrumental/io.earcam.instrumental.module.auto "0.1.0"]
   ;[io.earcam.instrumental/io.earcam.instrumental.compile.glue "0.1.0"]
   ;[io.earcam.instrumental/io.earcam.instrumental.lade "0.1.0"]
   ;[io.earcam.instrumental/io.earcam.instrumental.archive "0.1.0"]
   ;[io.earcam.instrumental/io.earcam.instrumental.archive.glue "0.1.0"]
   ;[io.earcam.instrumental/io.earcam.instrumental.archive.sign "0.1.0"]
   ;[io.earcam.instrumental/io.earcam.instrumental.compile "0.1.0"]
   ;[io.earcam.instrumental/io.earcam.instrumental.compile.glue "0.1.0"]
   ;[io.earcam.utilitarian/io.earcam.utilitarian.security "1.2.0"]

   [com.google.guava/guava "33.0.0-jre"]

   [io.smallrye.reactive/mutiny "2.5.8"]
   [org.awaitility/awaitility "4.2.0"]

   [funcool/promesa "11.0.678"]
   [party.donut/system "1.0.255"]
   ;[integrant "0.8.1"] ; maybe
   [missionary/missionary "b.36"]
   [metosin/malli "0.17.0"]

   ;; https://github.com/clj-commons/aleph/commit/998a32a5ed9ada581fa73a389fbb0df43e8dd2d2
   [aleph "0.8.3"]
   [gloss "0.2.6"]
   [metosin/reitit "0.5.18"]
   ;[org.clojure/core.async "1.6.673"]

   [org.reactivestreams/reactive-streams "1.0.4"]
   [io.reactivex.rxjava3/rxjava "3.1.8"]

   [org.apache.commons/commons-lang3 "3.18.0"]
   [commons-codec/commons-codec "1.16.1"]

   ;;[com.google.auto.service/auto-service "1.1.1"]
   [org.projectlombok/lombok "1.18.34"]

   [org.jetbrains/annotations "24.1.0"]
   ;; [org.checkerframework/checker-qual "3.42.0"]

   [org.slf4j/slf4j-api "1.7.25"]
   [org.slf4j/slf4j-simple "1.7.25"]
   
   [junit/junit "4.13.2" :scope "test"]]

  :source-paths
  ["src/main/clojure"]
  :test-paths
  ["src/test/clojure"]
  :java-source-paths
  ["src/main/java"
   "src/test/java"]

  :main ^:skip-aot evaleted-lein.core
  :target-path "target/%s"
  ; :jvm-opts [ "-cp" "./out/production/evelated-potential/:target/dependency/annotations-24.1.0.jar"]

  :aot :all
  :profiles
  {:uberjar {;;:aot      :all
             :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
   :dev     {:source-paths ["dev"]
             :dependencies [;;
                           ;[org.clojure/tools.namespace "0.2.3"]
                           ;[org.clojure/java.classpath "0.2.0"]
                             ]
             :plugins      [;;
                           ;[com.jakemccrary/lein-test-refresh "0.25.0"]
                           ;[venantius/ultra "0.6.0"]
                             ]}}

  :plugins
  [;[dev.weavejester/lein-cljfmt "0.12.0"]
  ;[lein-marginalia "0.9.2"]
  ;[lein-javac "1.2.1-SNAPSHOT"]
    ]

  :pom-addition
  [:properties
   [:maven.compiler.source "17"]
   [:maven.compiler.target "17"]
   [:maven.compiler.release "17"]]

  :clean-targets ^{:protect false} ["target/public"]
  )

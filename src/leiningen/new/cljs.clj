(ns leiningen.new.cljs
  (:require [leiningen.new.common :refer :all]))

(def cljs-assets
  [["src/cljs/{{sanitized}}/core.cljs" "cljs/src/cljs/core.cljs"]
   ["src/cljs/{{sanitized}}/ajax.cljs" "cljs/src/cljs/ajax.cljs"]
   "src/cljc/{{sanitized}}"
   ["src/cljc/{{sanitized}}/validation.cljc" "cljs/src/cljc/validation.cljc"]
   ["test/cljs/{{sanitized}}/doo_runner.cljs" "cljs/test/cljs/doo_runner.cljs"]
   ["test/cljs/{{sanitized}}/core_test.cljs" "cljs/test/cljs/core_test.cljs"]
   ["env/dev/cljs/{{sanitized}}/dev.cljs" "cljs/env/dev/cljs/app.cljs"]
   ["env/dev/clj/{{sanitized}}/figwheel.clj" "cljs/env/dev/clj/figwheel.clj"]
   ["env/prod/cljs/{{sanitized}}/prod.cljs" "cljs/env/prod/cljs/app.cljs"]
   ["resources/templates/home.html" "cljs/templates/home.html"]
   ["resources/templates/error.html" "core/resources/templates/error.html"]])

(def cljs-version "1.8.51")

(def figwheel-version "0.5.3-1")

(def cljs-dependencies
  [['org.clojure/clojurescript cljs-version :scope "provided"]
   ['reagent "0.6.0-alpha2"]
   ['reagent-utils "0.1.8"]
   ['secretary "1.2.3"]
   ['cljs-ajax "0.5.4"]])

(def source-paths
  ["src/cljc"])

(def resource-paths
  ["target/cljsbuild"])

(def cljs-plugins
  [['lein-cljsbuild "1.1.3"]])

(def cljs-dev-plugins
  [['lein-figwheel figwheel-version]
   ['lein-doo "0.1.6"]
   ['org.clojure/clojurescript cljs-version]])

(def clean-targets [:target-path
                    [:cljsbuild :builds :app :compiler :output-dir]
                    [:cljsbuild :builds :app :compiler :output-to]])

(def cljs-dev-dependencies
  [['lein-figwheel figwheel-version]
   ['lein-doo "0.1.6"]
   ['com.cemerick/piggieback "0.2.2-SNAPSHOT"]])

(def cljs-build
  {:builds {:app {:source-paths ["src/cljc" "src/cljs"]
                  :compiler     {:output-to    "target/cljsbuild/public/js/app.js"
                                 :output-dir   "target/cljsbuild/public/js/out"
                                 :externs      ["react/externs/react.js"]
                                 :pretty-print true}}}})

(def cljs-uberjar
  {:prep-tasks ["compile" ["cljsbuild" "once"]]
   :cljsbuild  {:builds {:app
                         {:source-paths ["env/prod/cljs"]
                          :compiler     {:optimizations :advanced
                                         :pretty-print  false
                                         :closure-warnings
                                                        {:externs-validation :off
                                                         :non-standard-jsdoc :off}}}}}})

(defn cljs-dev [{:keys [project-ns]}]
  {:cljsbuild {:builds
               {:app
                {:source-paths ["env/dev/cljs"]
                 :compiler     {:main       (str project-ns ".app")
                                :asset-path "/js/out"
                                :optimizations :none
                                :source-map true}}
                :test {:source-paths ["src/cljc" "src/cljs" "test/cljs"]
                       :compiler {:output-to "target/test.js"
                                  :main (str project-ns ".doo-runner")
                                  :optimizations :whitespace
                                  :pretty-print true}}}}})

(def cljs-test
  {:build "test"})

(defn figwheel [{:keys [project-ns]}]
  {:http-server-root "public"
   :nrepl-port       7002
   :css-dirs         ["resources/public/css"]})

(defn cljs-features [[assets options :as state]]
  (if (some #{"+cljs"} (:features options))
    [(into (remove-conflicting-assets assets ".html") cljs-assets)
     (-> options
         (update :dependencies #(remove #{['org.webjars/jquery "2.2.2"]} %))
         (append-options :dependencies cljs-dependencies)
         (append-options :plugins cljs-plugins)
         (append-options :source-paths source-paths)
         (append-options :resource-paths resource-paths)
         (append-options :dev-dependencies cljs-dev-dependencies)
         (append-options :dev-plugins cljs-dev-plugins)
         (update-in [:clean-targets] (fnil into []) clean-targets)
         (assoc
           :cljs true
           :cljs-build (indent root-indent cljs-build)
           :cljs-test cljs-test
           :figwheel (indent dev-indent (figwheel options))
           :cljs-dev (unwrap-map (indent dev-indent (cljs-dev options)))
           :cljs-uberjar (unwrap-map (indent uberjar-indent cljs-uberjar))))]
    state))

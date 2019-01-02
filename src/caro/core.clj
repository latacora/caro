(ns caro.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [com.rpl.specter :as sr]
            [cheshire.core :as json]
            [byte-streams :as bs]
            [taoensso.timbre :as log])
  (:gen-class))

(defn ^:private run
  [f paths]
  (let [input-objects (json/parsed-seq *in*)
        procs (for [sel (map edn/read-string paths)]
                {::selector sel
                 ::subprocess (.exec (Runtime/getRuntime) f)})]
    (doseq [obj input-objects
            {::keys [selector subprocess]} procs
            :let [v (sr/select-one selector obj)]]
      (->> subprocess .getOutputStream io/writer (json/generate-stream v)))

    (let [template (first input-objects)
          xformeds (->> procs
                       (map (fn [{::keys [subprocess]}]
                              (-> subprocess .getOutputStream .close)
                              (-> subprocess .getInputStream bs/to-reader json/parsed-seq)))
                       (apply map vector)
                       (map (fn [outputs]
                              (let [path (->> outputs
                                              (map (fn [{::keys [selector]} new-val]
                                                     (conj selector (sr/terminal-val new-val)))
                                                   procs)
                                              (apply sr/multi-path))]
                                (sr/multi-transform* path template)))))]
      (doseq [xformed xformeds]
        (json/generate-stream xformed *out*)))))

(defn -main
  [f & paths]
  (log/with-config
    (-> log/example-config
        (assoc-in [:appenders :println] (log/println-appender :stream :*err*)))
    (run f paths)))

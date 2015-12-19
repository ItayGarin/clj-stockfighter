(ns stockfighter.core
  (:require [cheshire.core :refer [parse-string]]
            [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]))

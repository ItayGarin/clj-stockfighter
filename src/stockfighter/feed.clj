(ns stockfighter.feed
  (:require [cheshire.core :refer [parse-string]]
            [clojure.core.async :as as]
            [com.stuartsierra.component :as component]
            [manifold.stream :as s]
            [stockfighter.api :refer :all]))

(defn- go-feed [{:keys [websocket out-chan on?]}]
  (as/go-loop []
    (when @on?
      (when-let [msg @(s/take! websocket)]
        (when-let [parsed (parse-string msg true)]
          (as/>! out-chan parsed)))
      (recur))))

(defrecord WsFeed [websocket out-chan]
  component/Lifecycle
  (start [c]
    (let [started-c (assoc c :on? (atom true))]
      (go-feed started-c)
      started-c))
  (stop [c]
    (reset! (:on? c) false)
    c))

(ns stockfighter.poll-feed
  (:require [clojure.core.async :as as]
            [com.stuartsierra.component :as component]))

(defn- go-poll-feed [{:keys [out-chan interval form on?] :as c}]
  (as/go-loop []
    (when @on?
      (as/>! out-chan (eval form))
      (Thread/sleep interval)
      (recur))))

(defrecord PollFeed [out-chan interval form]
  component/Lifecycle
  (start [c]
    (let [c-with-on? (assoc c :on? (atom true))]
      (go-poll-feed c-with-on?)
      c-with-on?))
  (stop [c]
    (reset! (:on? c) false)
    c))

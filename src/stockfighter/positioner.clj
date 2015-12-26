(ns stockfighter.positioner
  (:require [clojure.core.async :as as]
            [com.stuartsierra.component :as component]))

(def ^:private ^:const initial-balance {:pos 0 :cash 0})
(def ^:private direction->func {"buy" + "sell" -})

(defn- reduce-func-arg-vectors [init vectors]
  "Reduces a vector of [func arg] vectors"
  (reduce (fn [accum [func arg]]
            (func accum arg))
          init vectors))

(defn get-shares-position
  "Returns the amount of shares in a particular stock.
  This functions takes the current position, and a vector of order status maps"
  [pos orders]
  (let [fills (map #(vector (get direction->func (:direction %))
                            (:totalFilled %))
                   orders)]
    (reduce-func-arg-vectors pos fills)))

(defn- fills->cash-sum
  "Returns the accumulated sum of the fills vector"
  [fills]
  (let [sub-sums (map #(* (:qty %) (:price %)) fills)]
    (reduce + sub-sums)))

(defn get-cash-amount
  "Returns our cash balance (can be positive or negative!).
  This functions takes the current position, and a vector of order status maps"
  [cash orders]
  (let [fills (map #(vector (get direction->func (:direction %))
                            (fills->cash-sum (:fills %)))
                   orders)]
    (reduce-func-arg-vectors cash fills)))

(defn get-balance [orders]
  "Returns an updated cash+pos balance hash-map.
  This functions takes the current balance, and a vector of order status maps"
  {:pos (get-shares-position 0 orders)
   :cash (get-cash-amount 0 orders)})

(defn- go-monitor [{:keys [in-chan balance-atom on?] :as c}]
  (as/go-loop []
    (when-let [orders (:orders (as/<! in-chan))]
      (when @on?
        (reset! balance-atom (get-balance orders))
        (recur)))))

(defrecord Positioner [in-chan balance-atom]
  component/Lifecycle
  (start [c]
    (let [c-with-on? (assoc c :on? (atom true))]
      (go-monitor c-with-on?)
      c-with-on?))
  (stop [c]
    (reset! (:on? c) false)
    c))

(defn make-positioner [in-chan]
  (let [balance-atom (atom initial-balance)]
    (->Positioner in-chan balance-atom)))

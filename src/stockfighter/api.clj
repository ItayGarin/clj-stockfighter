(ns stockfighter.api
  (:require [cheshire.core :refer [parse-string]]
            [clj-http.client :as client]
            [stockfighter.url-builder :refer :all]))

;;;;;;;;;;;;;;;;;;;;; PRIVATE ;;;;;;;;;;;;;;;;;;;;;;;;

(defn- assoc-auth [params key]
  (assoc-in params [:headers "X-Starfighter-Authorization"] key))

(defn- parse-body [msg]
  (parse-string (:body msg) true))

(defn- req-n-validate [func api-key url params]
  "Performs `func` on the `url` and validates the response.
   Returns a map {:ok? true/false :body ...}"
  (let [auth-params (assoc-auth params api-key)
        resp (func url auth-params)
        body (parse-body resp)
        http-ok? (= 200 (:status resp))
        req-ok? (:ok body)]
    {:ok? (and http-ok? req-ok?)
     :body body}))

(defn- post-n-validate
  "Performs a POST on the `url` and validates the response.
   Returns a map {:ok? true/false :body ...}"
  [api-key url params]
  (req-n-validate client/post api-key url params))

(defn- delete-n-validate
  "Performs a DELETE on the `url` and validates the response.
   Returns a map {:ok? true/false :body ...}"
  ([api-key url]
   (delete-n-validate api-key url {}))
  ([api-key url params]
   (req-n-validate client/delete api-key url params)))

(defn- get-n-validate
  "Performs a GET on the `url` and validates the response.
   Returns a map {:ok? true/false :body ...}"
  ([url]
   (get-n-validate "" url))
  ([api-key url]
   (req-n-validate client/get api-key url {})))

(defn- alive? [heartbeat-url]
  (:ok? (get-n-validate heartbeat-url)))


;;;;;;;;;;;;;;;;;;;;; PUBLIC ;;;;;;;;;;;;;;;;;;;;;;;;

(defn api-up? []
  "Performs an API heartbeat GET request."
  (alive? (build-api-heartbeat-url)))

(defn venue-up? [venue]
  "Performs a venue heartbeat GET request."
  (alive? (build-venue-heartbeat-url venue)))

(defn get-venue-stocks [venue]
  "Requests all the stocks in a venue (via GET).
   Returns a vector of stock maps -
   [{:name x :symbol x} {:name y :symbol y}]"
  (->> (build-venue-stocks-url venue)
       (get-n-validate)
       (:body)))

(defn get-stock-quote [venue stock]
  "Requests a stock's quote (via GET).
   Returns a quote hash-map"
  (->> (build-stock-quote-url venue stock)
       (get-n-validate)
       (:body)))

(defn get-order-status [api-key venue stock order-id]
  "Requests an order's status (via GET).
   Returns a status hash-map"
  (->> (build-stock-order-url venue stock (str order-id))
       (get-n-validate api-key)
       (:body)))

(defn get-account-all-orders-status [api-key venue account]
  "Requests the status of all the orders in an account (via GET).
   Returns a vector of status hash-maps"
  (->> (build-account-all-orders-url venue account)
       (get-n-validate api-key)
       (:body)))

(defn get-account-stock-orders-status [api-key venue account stock]
  "Requests the status of all the orders in an account for a particular stock (via GET).
   Returns a vector of status hash-maps"
  (->> (build-account-stock-orders-url venue account stock)
       (get-n-validate api-key)
       (:body)))

(defn cancel-order [api-key venue stock order-id]
  "Attempts to delete an order (via DELETEF).
   Returns the order's status hash-map"
  (->> (build-stock-order-url venue stock (str order-id))
       (delete-n-validate api-key)
       (:body)))

(defn post-order [api-key venue stock account direction qty price orderType]
  "Attempts to post an order on a stock (via POST).
   Returns the order's status hash-map"
  (let [url (build-stock-orders-url venue stock)
        form {:account account
              :venue venue
              :stock stock
              :price price
              :qty qty
              :direction direction
              :orderType orderType}]
    (:body (post-n-validate api-key url {:form-params form
                                         :content-type :json}))))

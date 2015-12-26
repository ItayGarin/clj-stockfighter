(ns stockfighter.api
  (:require [aleph.http :as a]
            [cheshire.core :refer [parse-string]]
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

(defn- make-ws
  "Returns a websocket client that can be manipulated using manifold.stream API"
  [url]
  (deref (a/websocket-client url)))

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

(defn cancel-order [api-key {:keys [venue stock id]}]
  "Attempts to delete an order (via DELETEF).
   Returns the order's status hash-map"
  (->> (build-stock-order-url venue stock (str id))
       (delete-n-validate api-key)
       (:body)))

(defn make-order
  "Creates an order hash-map that's compatible with (post-order)"
  [venue stock account direction qty price orderType]
  {:account account
   :venue venue
   :stock stock
   :price price
   :qty qty
   :direction direction
   :orderType orderType})

(defn post-order [api-key {:keys [venue stock] :as order}]
  "Attempts to post an order on a stock (via POST).
   Returns the order's status hash-map"
  (let [url (build-stock-orders-url venue stock)]
    (:body (post-n-validate api-key url {:form-params order
                                         :content-type :json}))))

(defn make-venue-tickertape-ws
  "Returns a websocket client that can be manipulated using manifold.stream API"
  [venue account]
  (make-ws (build-ws-venue-tickertape-url venue account)))

(defn make-stock-tickertape-ws
  "Returns a websocket client that can be manipulated using manifold.stream API"
  [venue account stock]
  (make-ws (build-ws-stock-tickertape-url venue account stock)))

(defn make-venue-fills-ws
  "Returns a websocket client that can be manipulated using manifold.stream API"
  [venue account & additionals]
  (make-ws (build-ws-venue-fills-url venue account)))

(defn make-stock-fills-ws
  "Returns a websocket client that can be manipulated using manifold.stream API"
  [venue account stock]
  (make-ws (build-ws-stock-fills-url venue account stock)))

(defn gm-start-level [api-key level]
  (let [url (build-gm-level-url level)]
    (:body (post-n-validate api-key url {:content-type :json}))))

(defn gm-restart-level [api-key instance]
  (let [url (build-gm-instance-restart-url instance)]
    (:body (post-n-validate api-key url {:content-type :json}))))

(defn gm-stop-level [api-key instance]
  (let [url (build-gm-instance-stop-url instance)]
    (:body (post-n-validate api-key url {:content-type :json}))))

(defn gm-resume-level [api-key instance]
  (let [url (build-gm-instance-resume-url instance)]
    (:body (post-n-validate api-key url {:content-type :json}))))

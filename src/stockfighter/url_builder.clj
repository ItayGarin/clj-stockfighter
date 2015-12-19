(ns stockfighter.url-builder
  (:require [clojure.string :as s]))

;;;;;;;;;;;;;
;; PRIVATE ;;
;;;;;;;;;;;;;

(def ^:private api-uri "api.stockfighter.io/ob/api/")

(defn- remove-dup-slashes [s]
  (s/replace s #"/+" "/"))

(defn- cat-uri [& subs]
  "Construct a URI from multiple strings.
  Notice, it will remove duplicate slashes in substring!
  Thus, this builds a URI and not a URL"
  (->> subs
       (map s/trim)
       (s/join \/)
       (remove-dup-slashes)))

;;;;;;;;;;;;;;
;; API URLs ;;
;;;;;;;;;;;;;;

(defn build-api-url
  "https://api.stockfighter.io/ob/api/"
  [& additionals]
  (str "https://" (apply cat-uri api-uri additionals)))

(defn build-api-heartbeat-url
  "https://api.stockfighter.io/ob/api/heartbeat"
  []
  (build-api-url "heartbeat"))

;;;;;;;;;;;;;;;;
;; VENUE URLs ;;
;;;;;;;;;;;;;;;;

(defn build-venue-url
  "https://api.stockfighter.io/ob/api/venues/:venue/"
  [venue & additionals]
  (apply build-api-url "venues" venue additionals))

(defn build-venue-heartbeat-url
  "https://api.stockfighter.io/ob/api/venues/:venue/heartbeat"
  [venue]
  (build-venue-url venue "heartbeat"))

(defn build-venue-stocks-url
  "https://api.stockfighter.io/ob/api/venues/:venue/stocks"
  [venue & additionals]
  (apply build-venue-url venue "stocks" additionals))

;;;;;;;;;;;;;;;;
;; STOCK URLs ;;
;;;;;;;;;;;;;;;;

(defn build-stock-url
  "https://api.stockfighter.io/ob/api/venues/:venue/stocks/:stock/"
  [venue stock & additionals]
  (apply build-venue-stocks-url venue stock additionals))

(defn build-stock-quote-url
  "https://api.stockfighter.io/ob/api/venues/:venue/stocks/:stock/quote"
  [venue stock]
  (build-stock-url venue stock "quote"))

(defn build-stock-orders-url
  "https://api.stockfighter.io/ob/api/venues/:venue/stocks/:stock/orders"
  [venue stock & additionals]
  (apply build-stock-url venue stock "orders" additionals))

(defn build-stock-order-url
  "https://api.stockfighter.io/ob/api/venues/:venue/stocks/:stock/orders/:id"
  [venue stock order-id]
  (build-stock-orders-url venue stock order-id))

;;;;;;;;;;;;;;;;;;
;; ACCOUNT URLs ;;
;;;;;;;;;;;;;;;;;;

(defn build-account-url
  "https://api.stockfighter.io/ob/api/venues/:venue/accounts/:account/"
  [venue account & additionals]
  (apply build-venue-url venue "accounts" account additionals))

(defn build-account-all-orders-url
  "https://api.stockfighter.io/ob/api/venues/:venue/accounts/:account/orders"
  [venue account]
  (build-account-url venue account "orders"))

(defn build-account-stock-orders-url
  "https://api.stockfighter.io/ob/api/venues/:venue/accounts/:account/stocks/:stock/orders"
  [venue account stock]
  (build-account-url venue account "stocks" stock "orders"))

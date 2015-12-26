(ns stockfighter.url-builder
  (:require [clojure.string :as s]))

;;;;;;;;;;;;;
;; PRIVATE ;;
;;;;;;;;;;;;;

(def ^:private api-uri "api.stockfighter.io/ob/api/")
(def ^:private gm-uri "www.stockfighter.io/gm")

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

;;;;;;;;;;;;;;;;;;;;;
;; WEBSOCKETS URLs ;;
;;;;;;;;;;;;;;;;;;;;;

(defn build-ws-api-url
  "wss://api.stockfighter.io/ob/api/ws/"
  [& additionals]
  (str "wss://" (apply cat-uri api-uri "ws/" additionals)))

(defn build-ws-venue-tickertape-url
  "wss://api.stockfighter.io/ob/api/ws/:trading_account/venues/:venue/tickertape"
  [venue account & additionals]
  (apply build-ws-api-url account "venues" venue "tickertape" additionals))

(defn build-ws-stock-tickertape-url
  "wss://api.stockfighter.io/ob/api/ws/:trading_account/venues/:venue/tickertape/stocks/:stock"
  [venue account stock]
  (build-ws-venue-tickertape-url venue account "stocks" stock))

(defn build-ws-venue-fills-url
  "wss://api.stockfighter.io/ob/api/ws/:trading_account/venues/:venue/executions"
  [venue account & additionals]
  (apply build-ws-api-url account "venues" venue "executions" additionals))

(defn build-ws-stock-fills-url
  "wss://api.stockfighter.io/ob/api/ws/:trading_account/venues/:venue/executions/stocks/:symbol"
  [venue account stock]
  (build-ws-venue-fills-url venue account "stocks" stock))

;;;;;;;;;;;;;;;;;;;;;
;; GAMEMASTER URLs ;;
;;;;;;;;;;;;;;;;;;;;;

(defn build-gm-api-url
  "https://www.stockfighter.io/gm/"
  [& additionals]
  (str "https://" (apply cat-uri gm-uri additionals)))

(defn build-gm-level-url
  "https://www.stockfighter.io/gm/levels/:level"
  [level]
  (build-gm-api-url "levels" level))

(defn build-gm-instance-url
  "https://www.stockfighter.io/gm/instances/:instance"
  [instance & additionals]
  (apply build-gm-api-url "instances" instance additionals))

(defn build-gm-instance-restart-url
  "https://www.stockfighter.io/gm/instances/:instance/restart"
  [instance]
  (build-gm-instance-url instance "restart"))

(defn build-gm-instance-stop-url
  "https://www.stockfighter.io/gm/instances/:instance/stop"
  [instance]
  (build-gm-instance-url instance "stop"))

(defn build-gm-instance-resume-url
  "https://www.stockfighter.io/gm/instances/:instance/resume"
  [instance]
  (build-gm-instance-url instance "resume"))

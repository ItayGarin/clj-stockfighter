# clj-stockfighter

[![Clojars Project](http://clojars.org/clj-stockfighter/latest-version.svg)](http://clojars.org/clj-stockfighter)

## Overview

__clj-stockfighter__ provides an easy-to-use wrapping for [Stockfighter](https://www.stockfighter.io/)'s API.

I've set out to build this in Clojure as a part in my ongoing journey of learning this wonderful language.  
Thus, any feedback and contributions are more than welcome :)

I hope this lib will aid you in the quest of becoming the best stock fighter!

This library uses the MIT license => You can (and should!) go wild with this.

## Usage

### Table of Contents

- [How to Use in Your Project](#require)
- [API/Venue Up? (Heartbeat)](#api-venue-up)
- [Getting The Symbols of a Venue's Stocks](#get-venue-stocks)
- [Getting a Quote for a stock](#get-stock-quote)
- [Getting the Orderbook for a stock](#get-order-status)
- [Post/Cancel an Order](#post-cancel-order)
- [Websocket API](#websocket-api)
- [Game-Master API](#gamemaster-api)
- [Contribution / Contact](#contrib)
- [License](#license)

<div id='require'/>
### How to Use in Your Project

Add __clj-stockfighter__ to your lein project is easy as pie!  
Simply add `[clj-stockfighter "0.1.0-SNAPSHOT"]` to the `:dependencies` vector in your `project.clj`.

This might look like this -

```Clojure
:dependencies [[org.clojure/clojure "1.7.0"]
                [clj-stockfighter "0.1.0-SNAPSHOT"]]
```

Then, you could use it in your source files like so -
```Clojure
(ns solution-lvl1
  (:require [clj-stockfighter.api :as api]))
```

<div id='api-venue-up'/>
### API/Venue Up? (Heartbeat)

This is how you check if Stockfighter's API is up.  
Under the hood, this will perform a simple GET heartbeat request.  

__Reference__ - [API Heartbeat](https://starfighter.readme.io/docs/heartbeat)
```Clojure
(api-up?) 
;; => true
```

Similarly, This is how you check if a specific venue is up.  
In this example (and the following ones), I'll be using the "TESTEX" test venue.  
Once again, this will perform a GET heartbeat request.

__Reference__ - [Venue Heartbeat](https://starfighter.readme.io/docs/venue-healthcheck)

```Clojure
(defn venue-up? [venue])
;; => true
```

<div id='get-venue-stocks'/>
### Getting The Symbols of a Venue's Stocks

Pretty self-explanatory.  
Simply returns a standard response hash-map with a vector of symbols.

__Reference__ - [Stocks on a Venue](https://starfighter.readme.io/docs/list-stocks-on-venue)

```Clojure
(get-venue-stocks "TESTEX")

;; =>
 {:ok true,
 :symbols
 [{:name "Foreign Owned Occluded Bridge Architecture Resources",
   :symbol "FOOBAR"}]}
```

<div id='get-stock-quote'/>
### Getting a Quote for a stock

This returns a quote for a given stock in a venue.


```Clojure
(get-stock-quote "TESTEX" "FOOBAR")

;; =>
{:ok true,
 :venue "TESTEX",
 :symbol "FOOBAR",
 :quoteTime "2015-12-27T17:15:14.300598468Z",
 :lastTrade "2015-12-27T17:14:56.68753119Z",
 :last 50000,    ;; price of the last trade
 :lastSize 1,    ;; quantity of the last trade
 :ask 51000      ;; best price currently offered for the stock
 :askSize 20,    ;; aggregate size of all orders at the best ask
 :askDepth 10,   ;; aggregate size of *all asks*
 :bid 50000      ;; best price currently bid for the stock
 :bidSize 293,   ;; aggregate size of all orders at the best bid
 :bidDepth 766,  ;; aggregate size of *all bids*
 }
```

<div id='get-order-status'/>
### Getting the Orderbook for a stock

There are 3 different methods of obtaining the status of our orders ("Orderbook").
Here are a few examples for these different methods.

__Reference__ - [The Orderbook for a Stock](https://starfighter.readme.io/docs/get-orderbook-for-stock)

```Clojure
(def api-key "insert-your-key-here")

;; ex: All orders for a specific stock in an account
(get-account-stock-orders-status api-key "NDMEX" "HB2243235" "FOOBAR") 

;; ex: All orders in an account
(get-account-all-orders-status api-key "NDMEX" "HB2243235") ;; ex: All orders for an account

;; ex: Status for a specific order (based on the order-id)
(get-order-status "my-key" "TESTEX" "FOOBAR" order-id])

;; The output for (get-account-all-orders-status)
;; =>
{:ok true,
 :venue "NDMEX",
 :orders
 [{:open true,
   :symbol "FAC",
   :orderType "limit",
   :totalFilled 0,
   :account "HB2243235",
   :ts "2015-12-27T17:45:16.849640158Z",
   :id 8820,
   :ok true,
   :originalQty 10,
   :venue "NDMEX",
   :qty 10,
   :fills [],
   :price 1000,
   :direction "buy"}]}
```

<div id='post-cancel-order'/>
## Post/Cancel an Order

Here I'll explain how to post a new order and cancel a pending one.  

For convenience purposes the following is a utility function for creating an Order hash-map. 
This hash-map will serve as the argument of the cancel/post functions.

```Clojure
(def my-order
    (make-order "TESTEX" "FOOBAR" "ACCOUNT1234" "buy" 100 5000 "limit"))
;; =>
{:account "ACCOUNT1234",
 :venue "TESTEX",
 :stock "FOOBAR",
 :price 5000,
 :qty 100,
 :direction "buy",
 :orderType "limit"}
```

Once we have the order map, posting it is simple.  
Just give it to the `post-order` function (along with your api-key).


__Reference__ - [A New Order for a Stock](https://starfighter.readme.io/docs/place-new-order)

```Clojure
(post-order api-key my-order)
```

The same principle applies to canceling a pending order.  

The only gotcha here is that you have to `assoc` an :id to the order hash-map.
This let's the Stockfighter game-master know which order you're referring to.

__Reference__ - [Cancel An Order](https://starfighter.readme.io/docs/cancel-an-order)


```Clojure
;; Notice that we gotta add the :id to the order for canceling!
(cancel-order api-key (assoc my-order :id "13646"))
```

<div id='websocket-api'/>
## Websocket API

Once you feel comfortable with the above API you may want 
to stop polling the server and subscribe to a feed instead.

For that purpose, Stockfigter provides us with the Websocket API.
Here a few examples how to create and utilize these websockets.

Notice, that you don't need to provide an api-key to create these sockets.

__Reference__ - [Quotes (Ticker Tape) Websockets](https://starfighter.readme.io/docs/quotes-ticker-tape-websocket)

__Reference__ - [Executions (Fills) Websockets](https://starfighter.readme.io/docs/executions-fills-websocket)

```Clojure
(make-venue-tickertape-ws "TESTEX" "FOOBAR")
(make-stock-tickertape-ws "TESTEX" "ACCOUNT1234" "FOOBAR")
(make-venue-fills-ws "TESTEX" "FOOBAR")
(make-stock-fills-ws "TESTEX" "ACCOUNT1234" "FOOBAR")

;; To get a message from a websocket use the take! function manifold provides
(def msg 
    @(manifold.stream/take! websocket))
```

<div id='gamemaster-api'/>
## Game-Master API

The GM API is intended to serve those of you who are -
- Getting tired (like me) of starting, stopping or restarting a level.
- Are too lazy to copy and paste the venue, stock or account strings.

For these purposes, the library provides you with the following easy-to-use functions.  
Please read the reference forum post for more details.

__Reference__ - [The GM API](https://discuss.starfighters.io/t/the-gm-api-how-to-start-stop-restart-resume-trading-levels-automagically/143)

```Clojure
(defn gm-start-level [api-key level])
(defn gm-restart-level [api-key instance])
(defn gm-stop-level [api-key instance])
(defn gm-resume-level [api-key instance])
```

<div id='contrib'/>
## Contribution / Contact

Like I said I'd love to hear any feedback or input you might have.  
Feel free to submit a Pull Request or contact me at -  
thifixp@gmail.com

<div id='license'/>
## License

The MIT License (MIT)

Copyright (c) 2015 Itay Garin

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

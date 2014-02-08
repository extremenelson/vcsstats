(ns net.extreme-nelsons.state)

(def state (atom {}))

(defn get-state
  "Gets a keyed value from the system var"
  [key]
  (@state key))

(defn update-state [key val]
  (swap! state assoc key val))

(defn set-state [newstate]
  (reset! state newstate))

(defn get-whole-state []
  (@state))

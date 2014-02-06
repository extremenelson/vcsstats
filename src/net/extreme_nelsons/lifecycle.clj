(ns net.extreme-nelsons.lifecycle)

(defprotocol LifeCycle
  (start [this])
  (stop [this]))

(defn start-system [system]
  (doseq [s (->> system :order (map system))]
    (start s)))

(defn stop-system [system]
  (doseq [s (->> system :order (map system) reverse)]
    (stop s)))

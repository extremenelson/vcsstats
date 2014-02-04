(import
 '(java.awt.Graphics)
 '(java.swing JPanel JFrame))
(defn render [#^Graphics g]
  (doto g
    (.drawString "Hello World!" 10 20)))
(def panel (proxy [JPanel] []
             (paint [g] (render g))))
(def frame (doto (new JFrame)
             (.add panel)
             (.setBounds 100 100 100 60)
             (.setVisible true)))
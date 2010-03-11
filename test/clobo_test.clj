(ns clobo.test
  (:use clojure.test
	clobo))

(def car-path "test-indexes/cartag")
(def car-handlers
     [{:type :simple,
       :name "color"}
      {:type :range,
       :name "year",
       :auto-range true}
      {:type :range,
       :name "price",
       :auto-range true}
      {:type :multi-value,
       :name "tags"}
      {:type :range,
       :name "milage",
       :auto-range true}
      {:type :simple,
       :name "category"}
      {:type :path,
       :name "makemodel",
       :separator "/"}
      {:type :path,
       :name "city",
       :separator "/"}])

(def car-specs
     [{} {} {} {} {} {} {} {}])

(deftest test-car-search
  (let [idx (disk-index car-path)
	facet-map (into {} (map (fn [k v]
				  [(facet-handler k) (facet-spec v)])
				car-handlers car-specs))
	query "mp3"
	params {:default-field "contents"}
	result (browse idx facet-map query params)]
    (is (= (:num_hits result) 2967))
    (is (= (count (:hits result)) 10))
    (is (= (count (:facets result)) 8))))


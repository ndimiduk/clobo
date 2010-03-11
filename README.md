# clobo

Clojure bindings for bobo-browse. Specify query and facets as data structures
and iterate through results as a simple map.

## Usage

See the test for an example of browsing an index.

 (let [idx (disk-index "cartag")
       facet-map (into {} (map (fn [k v]
 				 [(facet-handler k) (facet-spec v)])
 			       car-handlers car-specs))
       query "mp3"
       params {:default-field "contents" :count 100}]
   (browse idx facet-map query params))


## Installation

FIXME: write

## License

FIXME: write

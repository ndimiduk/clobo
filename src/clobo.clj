(ns clobo
  (:import (java.util Arrays))
  (:import (org.apache.lucene.analysis Analyzer))
  (:import (org.apache.lucene.analysis.standard StandardAnalyzer))
  (:import (org.apache.lucene.index IndexReader))
  (:import (org.apache.lucene.queryParser QueryParser))
  (:import (org.apache.lucene.store NIOFSDirectory RAMDirectory))
  (:import (com.browseengine.bobo.api
	    BoboBrowser BoboIndexReader BrowseHit BrowseRequest
	    BrowseResult BrowseSelection FacetSpec FacetSpec$FacetSortSpec))
  (:import (com.browseengine.bobo.facets FacetHandler))
  (:import (com.browseengine.bobo.facets.impl
	    SimpleFacetHandler RangeFacetHandler MultiValueFacetHandler
	    PathFacetHandler)))

(defn disk-index
  [path]
  (NIOFSDirectory/getDirectory path))

;(defn memory-index
;  []
;  (RAMDirectory.))

(defn- index-reader
  [index]
  (IndexReader/open index true))

(defn- bobo-index-reader
  "Create a BoboIndexReader from an Index and collection of FacetHandlers"
  [#^IndexReader index-reader handlers]
  (BoboIndexReader/getInstance index-reader (Arrays/asList (into-array FacetHandler handlers))))

(defn facet-spec
  ([] (facet-spec {}))
  ([{:keys [min-hit-count max-count expand-selection order], :as args}]
     (let [spec (FacetSpec.)]
       (when (contains? args :min-hit-count)
	 (.setMinHitCount spec min-hit-count))
       (when (contains? args :max-count)
	 (.setMaxCount spec max-count))
       (when (contains? args :expand-selection)
	 (.setExpandSelection spec expand-selection))
       (when (and (contains? args :order) (= :desc order))
	 (.setOrderBy spec FacetSpec$FacetSortSpec/OrderHitsDesc))
       spec)))

(defmulti facet-handler :type)

(defmethod facet-handler :simple
  [{:keys [handler name index-field-name], :as args}]
  (if (contains? args :index-field-name)
    (SimpleFacetHandler. name index-field-name)
    (SimpleFacetHandler. name)))

(defmethod facet-handler :range
  [{:keys [handler name auto-range], :as args}]
  (RangeFacetHandler. name auto-range))

(defmethod facet-handler :path
  [{:keys [handler name separator], :as args}]
  (let [p (PathFacetHandler. name)]
    (when (contains? args :separator)
      (.setSeparator p separator))
    p))

(defmethod facet-handler :multi-value
  [{:keys [handler name], :as args}]
  (MultiValueFacetHandler. name))

(defn- browse-request
  [query facet-map {:keys [count offset default-field],
		    :or {count 10, offset 0, default-field "body"}}]
  (let [br (BrowseRequest.)
	q (QueryParser. default-field (StandardAnalyzer.))]
    (.setCount br count)
    (.setOffset br offset)
    (.setQuery br (. q parse query))
    (doall (for [[handler spec] facet-map]
	     (.setFacetSpec br (.getName handler) spec)))
    br))

(defn- document->map
  [document]
  (-> (into {}
	    (for [f (.getFields document)]
	      [(keyword (.name f)) (.stringValue f)]))
      (dissoc :_content)))

(defn- browsehit->map
  [#^BrowseHit hit]
  (into {}
	[[:docid (.getDocid hit)]
	 [:fields (into {}
			(for [[name values] (.getFieldValues hit)]
			  [name (seq values)]))]
	 [:score (.getScore hit)]
	 [:stored-fields (document->map (.getStoredFields hit))]]))

(defn- facet-map->map
  [facet-map]
  (into {}
	(for [[name facet-list] facet-map]
	  [name (into {}
		      (for [facet (.getFacets facet-list)]
			[(.getValue facet) (.getHitCount facet)]))])))

(defn- browse-result->map
  [#^IndexReader reader #^BrowseResult result]
  (into {}
	[[:num_hits (.getNumHits result)]
	 [:hits (for [hit (.getHits result)]
		  ((fn []
		     (.setStoredFields hit (.document reader (.getDocid hit)))
		     (browsehit->map hit))))]
	 [:facets (facet-map->map (.getFacetMap result))]]))

(defn search
  [index facet-map query params]
  (let [l-reader (index-reader index)
	b-reader (bobo-index-reader l-reader (keys facet-map))
	br     (browse-request query facet-map params)]
    (browse-result->map l-reader (.browse (BoboBrowser. b-reader) br))))
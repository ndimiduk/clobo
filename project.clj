(defproject clobo "0.0.1"
  :description "Clojure bindings for Bobo-Browse"
  :repositories {"javax" "http://repository.jboss.com/maven2",
		 "jmx" "http://simile.mit.edu/maven"}
  :dependencies [[org.clojure/clojure "1.1.0-alpha-SNAPSHOT"]
		 [org.clojure/clojure-contrib "1.0-SNAPSHOT"]
		 [log4j "1.2.15" :exclusions [javax.mail/mail
					      javax.jms/jms
					      com.sun.jdmk/jmxtools
					      com.sun.jmx/jmxri]]
		 [com.browseengine/bobo-browse "2.0.7"]
		 [org.apache.lucene/lucene-core "2.4.1"]
		 [fastutil/fastutil "5.0.9"]]
  :dev-dependencies [[leiningen/lein-swank "1.1.0"]])

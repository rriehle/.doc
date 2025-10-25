#!/usr/bin/env bb

(ns ^:clj-kondo/ignore doc-core
  "Core library for documentation keyword management.
   Provides config loading, keyword extraction, and cross-reference support.
   Delegates to config-core.bb for configuration management."
  (:require [babashka.fs :as fs]
            [clojure.edn :as edn]
            [clojure.string :as str]))

;; Load the shared config-core library
(def lib-dir (str (System/getenv "HOME") "/.lib"))
(load-file (str lib-dir "/config-core.bb"))

;; Import core functions from config-core
(def discover-project-root config-core/discover-project-root)
(def find-git-root config-core/find-git-root)
(def load-edn-file config-core/load-edn-file)
(def deep-merge config-core/deep-merge)
(def merge-configs config-core/merge-configs)
(def expand-path config-core/expand-path)
(def get-config-value config-core/get-config-value)

;; ============================================================================
;; Configuration Loading
;; ============================================================================

(defn load-config
  "Load and merge documentation configuration.
   Uses config-core.bb with :doc system type.

   Args:
     project-root-override: (optional) Override automatic project root discovery

   Returns:
     {:config {...}           ; Merged configuration
      :system-type :doc        ; System type
      :project-root \"...\"    ; Absolute path to project root
      :sources {:global \"...\" ; Path to global config
                :project \"...\"}  ; Path to project config (or nil)
      }"
  ([]
   (load-config nil))
  ([project-root-override]
   (config-core/load-config :doc project-root-override)))

(defn doc-config
  "Get the :doc section of the config.
   Convenience accessor for doc scripts.

   Args:
     config-result: Result from load-config

   Returns:
     :doc section of config map"
  [config-result]
  (get-config-value config-result [:doc]))

(defn resolve-doc-path
  "Resolve documentation directory path from config.

   Args:
     config-result: Result from load-config

   Returns:
     Absolute path to doc directory as string"
  [config-result]
  (config-core/resolve-path config-result [:doc :path]))

(defn resolve-taxonomy-path
  "Resolve taxonomy file path from config.

   Args:
     config-result: Result from load-config

   Returns:
     Absolute path to taxonomy file as string"
  [config-result]
  (config-core/resolve-path config-result [:doc :taxonomy]))

(defn resolve-template-dir
  "Resolve template directory path from config.

   Args:
     config-result: Result from load-config

   Returns:
     Absolute path to template directory as string"
  [config-result]
  (config-core/resolve-path config-result [:doc :template-dir]))

;; ============================================================================
;; Keyword Extraction
;; ============================================================================

(defn extract-keywords-from-content
  "Extract keywords from markdown content.
   Supports both inline [:keyword1 :keyword2] and bracket notation.

   Args:
     content: String content to search

   Returns:
     Set of keywords (as Clojure keywords)"
  [content]
  (->> (re-seq #"\[:([^\]]+)\]" content)
       (map second)
       (mapcat #(str/split % #"\s+"))
       (map str/trim)
       (map (fn [s] (str/replace s ":" "")))
       (filter seq)
       (map keyword)
       distinct
       set))

(defn extract-keywords-from-file
  "Extract keywords from a markdown file.

   Args:
     file-path: Path to markdown file

   Returns:
     Set of keywords (as Clojure keywords), or empty set on error"
  [file-path]
  (try
    (let [content (slurp file-path)]
      (extract-keywords-from-content content))
    (catch Exception e
      (println "Warning: Error reading" file-path ":" (.getMessage e))
      #{})))

;; ============================================================================
;; Cross-Reference Extraction
;; ============================================================================

(defn extract-cross-refs
  "Extract ADR, requirement, and RunNotes references from content.

   Supports:
   - ADR references: [ADR-00042] or ADR-00042
   - Requirement references: [REQ-CAT-001] or REQ-CAT-001
   - RunNotes references: [RunNotes-2025-01-15-Topic] or RunNotes-2025-01-15-Topic

   Args:
     content: String content to search

   Returns:
     {:adr-refs #{\"00042\" ...}
      :req-refs #{\"REQ-CAT-001\" ...}
      :runnote-refs #{\"RunNotes-2025-01-15-Topic\" ...}}"
  [content]
  {:adr-refs (->> (re-seq #"(?:\[)?ADR-(\d{5})(?:\])?" content)
                  (map second)
                  (into #{}))
   :req-refs (->> (re-seq #"(?:\[)?(REQ-[A-Z]+-[A-Z0-9-]+)(?:\])?" content)
                  (map second)
                  (into #{}))
   :runnote-refs (->> (re-seq #"(?:\[)?(RunNotes-\d{4}-\d{2}-\d{2}-[^\]]+)(?:\])?" content)
                      (map second)
                      (into #{}))})

(defn extract-cross-refs-from-file
  "Extract cross-references from a markdown file.

   Args:
     file-path: Path to markdown file

   Returns:
     Map with :adr-refs, :req-refs, :runnote-refs sets, or empty sets on error"
  [file-path]
  (try
    (let [content (slurp file-path)]
      (extract-cross-refs content))
    (catch Exception e
      (println "Warning: Error reading" file-path ":" (.getMessage e))
      {:adr-refs #{}
       :req-refs #{}
       :runnote-refs #{}})))

;; ============================================================================
;; File Discovery
;; ============================================================================

(defn find-markdown-files
  "Find all markdown files in a directory tree.

   Args:
     dir: Directory path to search
     excluded-patterns: (optional) Set of patterns to exclude

   Returns:
     Sequence of file paths as strings"
  ([dir]
   (find-markdown-files dir #{}))
  ([dir excluded-patterns]
   (when (fs/exists? dir)
     (->> (file-seq (fs/file dir))
          (filter #(str/ends-with? (str %) ".md"))
          (map str)
          (filter (fn [path]
                    (let [filename (fs/file-name path)]
                      (not (some #(str/includes? filename %) excluded-patterns)))))))))

(defn find-doc-files
  "Find documentation markdown files using config.

   Args:
     config-result: Result from load-config

   Returns:
     Sequence of file paths"
  [config-result]
  (let [doc-dir (resolve-doc-path config-result)
        doc-cfg (doc-config config-result)
        excluded (or (:excluded-patterns doc-cfg) #{})]
    (find-markdown-files doc-dir excluded)))

;; ============================================================================
;; Taxonomy Loading
;; ============================================================================

(defn load-taxonomy
  "Load valid keywords from taxonomy file.

   Taxonomy format supports:
   - Backtick-colon notation: `:keyword`
   - Inline backticks: `word` (converted to :word)

   Args:
     taxonomy-path: Path to taxonomy markdown file

   Returns:
     Set of valid keywords (as Clojure keywords), or nil if file doesn't exist"
  [taxonomy-path]
  (when (fs/exists? taxonomy-path)
    (try
      (let [content (slurp taxonomy-path)]
        (->> (re-seq #"`:([^`]+)`" content)
             (map second)
             (map keyword)
             (into #{})))
      (catch Exception e
        (println "Warning: Error loading taxonomy from" taxonomy-path ":" (.getMessage e))
        nil))))

;; ============================================================================
;; Project Context
;; ============================================================================

(defn load-project-context
  "Load complete project context for documentation analysis.

   Args:
     project-root: (optional) Project root path, or discover automatically

   Returns:
     {:config-result {...}     ; Full config result from load-config
      :doc-dir \"...\"          ; Absolute doc directory path
      :taxonomy-path \"...\"    ; Absolute taxonomy file path
      :taxonomy #{...}         ; Valid keywords from taxonomy (or nil)
      :excluded-patterns #{...}} ; Excluded file patterns"
  ([]
   (load-project-context nil))
  ([project-root]
   (let [config-result (load-config project-root)
         doc-dir (resolve-doc-path config-result)
         taxonomy-path (resolve-taxonomy-path config-result)
         taxonomy (load-taxonomy taxonomy-path)
         doc-cfg (doc-config config-result)
         excluded (or (:excluded-patterns doc-cfg) #{})]
     {:config-result config-result
      :doc-dir doc-dir
      :taxonomy-path taxonomy-path
      :taxonomy taxonomy
      :excluded-patterns excluded})))

;; ============================================================================
;; Validation Helpers
;; ============================================================================

(defn validate-keywords-against-taxonomy
  "Validate keywords against taxonomy.

   Args:
     keywords: Set of keywords to validate
     taxonomy: Set of valid keywords from taxonomy (or nil to skip validation)

   Returns:
     {:valid #{...}    ; Keywords present in taxonomy
      :invalid #{...}  ; Keywords not in taxonomy
      :all-valid? true|false}"
  [keywords taxonomy]
  (if (nil? taxonomy)
    {:valid keywords
     :invalid #{}
     :all-valid? true}
    (let [valid (filter taxonomy keywords)
          invalid (remove taxonomy keywords)]
      {:valid (set valid)
       :invalid (set invalid)
       :all-valid? (empty? invalid)})))

;; ============================================================================
;; Export for use by other scripts
;; ============================================================================

(def exports
  "Exported functions for use by doc commands"
  {;; Config loading
   :load-config load-config
   :doc-config doc-config
   :resolve-doc-path resolve-doc-path
   :resolve-taxonomy-path resolve-taxonomy-path
   :resolve-template-dir resolve-template-dir

   ;; Keyword extraction
   :extract-keywords-from-content extract-keywords-from-content
   :extract-keywords-from-file extract-keywords-from-file

   ;; Cross-reference extraction
   :extract-cross-refs extract-cross-refs
   :extract-cross-refs-from-file extract-cross-refs-from-file

   ;; File discovery
   :find-markdown-files find-markdown-files
   :find-doc-files find-doc-files

   ;; Taxonomy
   :load-taxonomy load-taxonomy

   ;; Project context
   :load-project-context load-project-context

   ;; Validation
   :validate-keywords-against-taxonomy validate-keywords-against-taxonomy

   ;; Re-export useful config-core functions
   :discover-project-root discover-project-root
   :find-git-root find-git-root
   :expand-path expand-path
   :get-config-value get-config-value})

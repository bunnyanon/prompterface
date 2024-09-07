(ns prompterface.image
  (:require [clojure.java.io :as io])
  (:import (java.util Arrays Base64)
           (java.nio ByteBuffer)
           (java.util.zip CRC32)))

(defn bytes->num [^"[B" array]
  (Integer/toUnsignedLong (.getInt (ByteBuffer/wrap array))))

(defn read-throw-eof [file ^Number buffer-size]
  (when (= (.read file buffer-size) -1) (throw (Exception. "EOF"))))

(defn verify-magic-numbers [file]
  (try
    (Arrays/equals (let [ba (byte-array 8)] (.read file ba) ba)
                   (byte-array [(long 0x89)
                                (long 0x50)
                                (long 0x4e)
                                (long 0x47)
                                (long 0x0d)
                                (long 0x0a)
                                (long 0x1a)
                                (long 0x0a)]))
    (catch Exception e (throw e))))

(defn read-header [file]
  (try
    (let [len-data (bytes->num (let [len (byte-array 4)] (read-throw-eof file len) len))
          content (byte-array len-data)
          header (byte-array 4)
          checksum (byte-array 4)
          bytes-read (+ len-data 4 4 4)]
      (read-throw-eof file header)
      (read-throw-eof file content)
      (read-throw-eof file checksum)
      (when (not (= (let [crc32checksum (new CRC32)]
                      (.update crc32checksum (byte-array (concat header content)))
                      (.getValue crc32checksum)) (long (bytes->num checksum))))
        (throw (Exception. "INVALID CRC32 CHECKSUM. MIGHT BE AN INDICATOR OF FILE CORRUPTION!")))
      (vector header content bytes-read))
    (catch Exception e (throw e))))

(defn find-chara [file]
  (let [reader (io/input-stream file)]
    (try
      (when (not (verify-magic-numbers reader)) (throw (Exception. "FILE IS NOT PNG")))
      (loop [[header content bytes-read] (read-header reader)
             start-position 8]                               ;; 8 magic bytes
        (if (and (Arrays/equals ^"[B" header (byte-array [(long \t) (long \E) (long \X) (long \t)]))
                 (= (take 6 content) '(99 104 97 114 97 0x00)))
          (vector
            (String. (.decode (Base64/getDecoder) ^String (apply str (map #(char (Byte/toUnsignedInt %1)) (drop 6 content)))))
            start-position
            bytes-read)
          (recur (read-header reader) (+ start-position bytes-read))))
     (catch Exception e (throw e)))))

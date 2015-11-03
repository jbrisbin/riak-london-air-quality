#!/usr/bin/env bash

./bin/riak-admin bucket-type create AirQualityDataRaw '{"props":{"n_val":3, "table_def":"CREATE TABLE AirQualityDataRaw (
  measurementDate timestamp not null,
  site varchar not null,
  species varchar not null,
  latitude float not null,
  longitude float not null,
  value float,
  PRIMARY KEY ((quantum(measurementDate, 1826, 'd')), measurementDate, site, species))"}}'

./bin/riak-admin bucket-type activate AirQualityDataRaw

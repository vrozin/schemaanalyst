DROP TABLE Stats;

DROP TABLE Station;

CREATE TABLE Station 
(
   ID INTEGER PRIMARY KEY, 
   CITY CHAR(20), 
   STATE CHAR(2), 
   LAT_N INTEGER NOT NULL CHECK (LAT_N BETWEEN 0 and 90), 
   LONG_W INTEGER NOT NULL CHECK (LONG_W BETWEEN SYMMETRIC 180 and -180)
);

CREATE TABLE Stats
(
  ID INTEGER REFERENCES STATION(ID), 
  MONTH INTEGER NOT NULL CHECK (MONTH BETWEEN 1 AND 12), 
  TEMP_F INTEGER NOT NULL CHECK (TEMP_F BETWEEN 80 AND 150), 
  RAIN_I INTEGER NOT NULL CHECK (RAIN_I BETWEEN 0 AND 100), 
  PRIMARY KEY (ID, MONTH)
);
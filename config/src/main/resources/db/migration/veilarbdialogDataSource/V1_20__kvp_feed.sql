ALTER TABLE FEED_METADATA ADD (SISTE_KVP_ID NUMBER(19, 0), SISTE_OPPFOLGING_ID TIMESTAMP(6));

UPDATE FEED_METADATA SET SISTE_KVP_ID = 0, SISTE_OPPFOLGING_ID = TIDSPUNKT_SISTE_ENDRING;

ALTER TABLE FEED_METADATA MODIFY SISTE_KVP_ID NUMBER(19, 0) NOT NULL;
ALTER TABLE FEED_METADATA MODIFY SISTE_OPPFOLGING_ID TIMESTAMP(6) NOT NULL;

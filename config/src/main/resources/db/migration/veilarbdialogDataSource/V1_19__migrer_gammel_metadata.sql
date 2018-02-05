UPDATE DIALOG d
SET ELDSTE_ULESTE_FOR_BRUKER = (SELECT min(h.SENDT)
                                FROM HENVENDELSE h
                                WHERE h.SENDT > d.LEST_AV_BRUKER_TID AND h.DIALOG_ID = d.DIALOG_ID AND h.AVSENDER_TYPE = 'VEILEDER');
UPDATE DIALOG d
SET ELDSTE_ULESTE_FOR_VEILEDER = (SELECT min(h.SENDT)
                                  FROM HENVENDELSE h
                                  WHERE h.SENDT > d.LEST_AV_VEILEDER_TID AND h.DIALOG_ID = d.DIALOG_ID AND h.AVSENDER_TYPE = 'BRUKER');

UPDATE DIALOG SET VENTER_PA_NAV_SIDEN = SISTE_UBEHANDLET_TID WHERE VENTER_PA_NAV_SIDEN IS NULL;
UPDATE DIALOG d
SET VENTER_PA_NAV_SIDEN = (SELECT min(h.SENDT)
                           FROM HENVENDELSE h
                           WHERE d.DIALOG_ID = h.DIALOG_ID AND h.SENDT > d.SISTE_FERDIGBEHANDLET_TID AND
                                 h.AVSENDER_TYPE = 'BRUKER') WHERE d.VENTER_PA_NAV_SIDEN IS NULL;

UPDATE DIALOG SET VENTER_PA_SVAR_FRA_BRUKER = SISTE_VENTE_PA_SVAR_TID;
UPDATE DIALOG d
SET VENTER_PA_SVAR_FRA_BRUKER = NULL
WHERE SISTE_VENTE_PA_SVAR_TID < (SELECT max(h.SENDT)
                                 FROM HENVENDELSE h
                                 WHERE d.DIALOG_ID = h.DIALOG_ID);


UPDATE DIALOG d
SET OPPDATERT = (SELECT max(h.SENDT)
                 FROM HENVENDELSE h
                 WHERE d.DIALOG_ID = h.DIALOG_ID);
UPDATE DIALOG SET OPPDATERT = VENTER_PA_NAV_SIDEN WHERE VENTER_PA_NAV_SIDEN > DIALOG.OPPDATERT;
UPDATE DIALOG SET OPPDATERT = VENTER_PA_SVAR_FRA_BRUKER WHERE VENTER_PA_SVAR_FRA_BRUKER > DIALOG.OPPDATERT;
UPDATE DIALOG SET OPPDATERT = ELDSTE_ULESTE_FOR_BRUKER WHERE ELDSTE_ULESTE_FOR_BRUKER > DIALOG.OPPDATERT;
UPDATE DIALOG SET OPPDATERT = ELDSTE_ULESTE_FOR_VEILEDER WHERE ELDSTE_ULESTE_FOR_VEILEDER > DIALOG.OPPDATERT;

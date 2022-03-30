CREATE TABLE ESKALERINGSVARSEL
(
    id                               number generated by default as identity primary key,
    aktor_id                         NVARCHAR2(255) NOT NULL,
    opprettet_av                     NVARCHAR2(255) NOT NULL,
    opprettet_dato                   TIMESTAMP      NOT NULL,
    tilhorende_dialog_id             number         NOT NULL,
    tilhorende_brukernotifikasjon_id number,
    opprettet_begrunnelse            CLOB,
    avsluttet_dato                   TIMESTAMP,
    avsluttet_av                     NVARCHAR2(255),
    avsluttet_begrunnelse            CLOB,
    constraint tilhorende_dialog_id_fk foreign key (tilhorende_dialog_id) references DIALOG (DIALOG_ID),
    constraint tilhorende_brukernotifikasjon_id_fk foreign key (tilhorende_brukernotifikasjon_id) references BRUKERNOTIFIKASJON (ID)
);
package no.nav.fo.veilarbdialog.db.dao;

import lombok.SneakyThrows;
import no.nav.fo.veilarbdialog.TestDataBuilder;
import no.nav.fo.veilarbdialog.domain.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Date;

import static java.lang.Thread.sleep;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class StatusDAOTest {

    private DialogDAO dialogDAO = mock(DialogDAO.class);
    private StatusDAO statusDAO = new StatusDAO(dialogDAO);

    @Test
    public void oppretterNyHenvendelse() {
        DialogData dialogData = TestDataBuilder.nyDialog();
        Date uniktTidspunkt = new Date();
        HenvendelseData henvendelseData = nyHenvendelseFraBruker(dialogData, uniktTidspunkt);

        Status status = statusDAO.getStatus(dialogData);
        status.eldsteUlesteForVeileder = uniktTidspunkt;
        status.venterPaNavSiden = uniktTidspunkt;

        statusDAO.nyHenvendelse(dialogData, henvendelseData);
        verify(dialogDAO).oppdaterStatus(status);
    }

    @Test
    public void oppretterNyHenvendelseFraVeileder() {
        DialogData dialogData = TestDataBuilder.nyDialog();
        Date uniktTidspunkt = new Date();
        HenvendelseData henvendelseData = nyHenvendelseFraVeileder(dialogData, uniktTidspunkt);

        Status status = statusDAO.getStatus(dialogData);
        status.eldsteUlesteForBruker = uniktTidspunkt;

        statusDAO.nyHenvendelse(dialogData, henvendelseData);
        verify(dialogDAO).oppdaterStatus(status);
    }

    @Test
    public void oppretterNyHenvendelsePaEksisterendeDialog() {
        DialogData dialogData = getDialogData();

        Date uniktTidspunkt = new Date();
        HenvendelseData henvendelseData = nyHenvendelseFraBruker(dialogData, uniktTidspunkt);

        Status status = statusDAO.getStatus(dialogData);

        statusDAO.nyHenvendelse(dialogData, henvendelseData);
        verify(dialogDAO).oppdaterStatus(status);
    }

    @Test
    public void markerSomLestAvVeileder() {
        DialogData dialogData = getDialogData();
        Status status = statusDAO.getStatus(dialogData);
        status.eldsteUlesteForVeileder = null;

        statusDAO.markerSomLestAvVeileder(dialogData);

        verify(dialogDAO, Mockito.only()).oppdaterStatus(status);
    }

    @Test
    public void markerSomLestAvBruker() {
        DialogData dialogData = getDialogData();
        Status status = statusDAO.getStatus(dialogData);
        status.eldsteUlesteForBruker = null;

        statusDAO.markerSomLestAvBruker(dialogData);

        verify(dialogDAO, only()).oppdaterStatus(status);
    }

    @Test
    public void fjernVenterPaNavSiden() {
        DialogData dialogData = getDialogData();
        Status status = statusDAO.getStatus(dialogData);
        DialogStatus dialogStatus = new DialogStatus(dialogData.getId(), false, true);
        status.venterPaNavSiden = null;

        statusDAO.oppdaterVenterPaNavSiden(dialogData, dialogStatus);

        verify(dialogDAO, only()).oppdaterStatus(status);
    }

    @Test
    public void oppdaterVenterPaNavSiden() {
        DialogData dialogData = getDialogData().withVenterPaNav(null);
        Status original = statusDAO.getStatus(dialogData);
        Date uniktTidspunkt = uniktTidspunkt();
        DialogStatus dialogStatus = new DialogStatus(dialogData.getId(), false, false);
        statusDAO.oppdaterVenterPaNavSiden(dialogData, dialogStatus);

        verify(dialogDAO, only()).oppdaterStatus(argThat(opppdatert -> verifyKunVenterPaNavEndret(original, uniktTidspunkt, opppdatert)));
    }

    private boolean verifyKunVenterPaNavEndret(Status original, Date uniktTidspunkt, Status opppdatert) {
        original.venterPaNavSiden = opppdatert.venterPaNavSiden;
        return opppdatert.venterPaNavSiden.after(uniktTidspunkt) && opppdatert.equals(original);
    }

    @Test
    public void fjernVenterPaSvarFraBruker() {
        DialogData dialogData = getDialogData();
        Status original = statusDAO.getStatus(dialogData);

        DialogStatus dialogStatus = new DialogStatus(dialogData.getId(), false, false);
        statusDAO.oppdaterVenterPaSvarFraBrukerSiden(dialogData, dialogStatus);

        verify(dialogDAO, only()).oppdaterStatus(argThat(oppdatert -> verifyKunVenterPaSvarFraBrukerFjernet(original, oppdatert)));
    }

    private boolean verifyKunVenterPaSvarFraBrukerFjernet(Status original, Status oppdatert) {
        original.venterPaSvarFraBruker = null;
        return oppdatert.equals(original);
    }

    @Test
    public void settVenterPaSvarFraBruker() {
        DialogData dialogData = getDialogData().withVenterPaSvarFraBruker(null);
        Status original = statusDAO.getStatus(dialogData);

        Date uniktTidspunkt = uniktTidspunkt();

        DialogStatus dialogStatus = new DialogStatus(dialogData.getId(), true, true);
        statusDAO.oppdaterVenterPaSvarFraBrukerSiden(dialogData, dialogStatus);

        verify(dialogDAO, only()).oppdaterStatus(argThat(oppdatert -> verifyKunVenterPaSvarFraBrukerFjernet(original, uniktTidspunkt, oppdatert)));
    }

    private boolean verifyKunVenterPaSvarFraBrukerFjernet(Status original, Date uniktTidspunkt, Status oppdatert) {
        original.venterPaSvarFraBruker = oppdatert.venterPaSvarFraBruker;
        return oppdatert.equals(original) && oppdatert.venterPaSvarFraBruker.after(uniktTidspunkt);
    }

    @Test
    public void settDialogTilHistorisk() {
        DialogData dialogData = getDialogData();

        Status status = new Status(dialogData.getId());
        status.setHistorisk(true);

        statusDAO.settDialogTilHistorisk(dialogData);
        verify(dialogDAO, only()).oppdaterStatus(status);

    }

    private DialogData getDialogData() {
        return TestDataBuilder.nyDialog()
                .withUlesteMeldingerForBruker(new Date())
                .withUlesteMeldingerForVeileder(new Date())
                .withVenterPaNav(new Date())
                .withVenterPaSvarFraBruker(new Date())
                .withHistorisk(false);
    }

    private HenvendelseData nyHenvendelseFraBruker(DialogData dialogData, Date uniktTidspunkt) {
        return TestDataBuilder
                .nyHenvendelse(dialogData.getId(), dialogData.getAktorId(), AvsenderType.BRUKER)
                .withSendt(uniktTidspunkt);
    }


    private HenvendelseData nyHenvendelseFraVeileder(DialogData dialogData, Date uniktTidspunkt) {
        return TestDataBuilder
                .nyHenvendelse(dialogData.getId(), dialogData.getAktorId(), AvsenderType.VEILEDER)
                .withSendt(uniktTidspunkt);
    }

    @SneakyThrows
    private Date uniktTidspunkt() {
        sleep(1);
        Date tidspunkt = new Date();
        sleep(1);
        return tidspunkt;
    }
}
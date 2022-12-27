package no.nav.fo.veilarbdialog.clients.veilarbperson;

import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient;
import no.nav.common.types.identer.Fnr;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.function.Supplier;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
public class VeilarbpersonClientImpl implements VeilarbpersonClient {

    private final String baseUrl;

    private final OkHttpClient client;

    private final Supplier<String> machineToMachineTokenProvider;

    public VeilarbpersonClientImpl(
            @Value("${application.veilarbperson.api.url}") String baseUrl,
            @Value("${application.veilarbperson.api.scope}") String scope,
            AzureAdMachineToMachineTokenClient tokenClient,
            OkHttpClient client
    ) {
        this.baseUrl = baseUrl;
        this.machineToMachineTokenProvider = () -> tokenClient.createMachineToMachineToken(scope);
        this.client = client;
    }

    @Override
    public Optional<Nivaa4DTO> hentNiva4(Fnr fnr) {
        String uri = String.format("%s/person/%s/harNivaa4", baseUrl, fnr.get());
        Request request = new Request.Builder()
                .url(uri)
                .header(AUTHORIZATION, "Bearer " + machineToMachineTokenProvider.get())
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);

            return RestUtils.parseJsonResponse(response, Nivaa4DTO.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Feil ved kall mot " + request.url(), e);
        }
    }

}

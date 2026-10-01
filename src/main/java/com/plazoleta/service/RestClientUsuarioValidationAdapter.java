package com.plazoleta.service;

import com.plazoleta.exception.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RestClientUsuarioValidationAdapter implements UsuarioValidationPort {

    private final RestClient restClient;
    private final String validationPath;

    public RestClientUsuarioValidationAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${app.usuarios.base-url}") String baseUrl,
            @Value("${app.usuarios.owner-validation-path}") String validationPath) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.validationPath = validationPath;
    }

    @Override
    public boolean existePropietarioValido(Long idPropietario) {
        try {
            Boolean valido = restClient.get()
                    .uri(validationPath, idPropietario)
                    .retrieve()
                    .body(Boolean.class);
            return Boolean.TRUE.equals(valido);
        } catch (HttpClientErrorException.NotFound exception) {
            return false;
        } catch (RestClientException exception) {
            throw new DomainException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No fue posible validar el propietario en el servicio de Usuarios");
        }
    }
}
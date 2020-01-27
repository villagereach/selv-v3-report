/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.report.service;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.report.exception.DataRetrievalException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseCommunicationServiceTest<T> {
  private static final String TOKEN = UUID.randomUUID().toString();
  protected static final String ACCESS_TOKEN = "access_token=" + TOKEN;

  @Mock
  protected RestTemplate restTemplate;

  @Mock
  private AuthorizationService authService;

  @Captor
  protected ArgumentCaptor<URI> uriCaptor;

  @Captor
  protected ArgumentCaptor<HttpEntity> entityCaptor;

  @Before
  public void setUp() throws Exception {
    mockAuth();
  }

  @After
  public void tearDown() throws Exception {
    checkAuth();
  }

  @Test
  public void shouldFindById() throws Exception {
    // given
    BaseCommunicationService<T> service = prepareService();
    UUID id = UUID.randomUUID();
    T instance = generateInstance();
    ResponseEntity<T> response = mock(ResponseEntity.class);

    // when
    when(response.getBody()).thenReturn(instance);
    when(restTemplate.exchange(
        any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(service.getResultClass())
    )).thenReturn(response);

    T found = service.findOne(id);

    // then
    verify(restTemplate).exchange(
        uriCaptor.capture(), eq(HttpMethod.GET), entityCaptor.capture(),
            eq(service.getResultClass())
    );

    URI uri = uriCaptor.getValue();
    String url = service.getServiceUrl() + service.getUrl() + id;

    assertThat(uri.toString(), is(equalTo(url)));
    assertThat(found, is(instance));

    assertAuthHeader(entityCaptor.getValue());
    assertThat(entityCaptor.getValue(), is(nullValue()));
  }

  @Test
  public void shouldReturnNullIfEntityCannotBeFoundById() throws Exception {
    // given
    BaseCommunicationService<T> service = prepareService();
    UUID id = UUID.randomUUID();

    // when
    when(restTemplate.exchange(
        any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(service.getResultClass())
    )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    T found = service.findOne(id);

    // then
    verify(restTemplate).exchange(
        uriCaptor.capture(), eq(HttpMethod.GET), entityCaptor.capture(),
            eq(service.getResultClass())
    );

    URI uri = uriCaptor.getValue();
    String url = service.getServiceUrl() + service.getUrl() + id;

    assertThat(uri.toString(), is(equalTo(url)));
    assertThat(found, is(nullValue()));

    assertAuthHeader(entityCaptor.getValue());
    assertThat(entityCaptor.getValue(), is(nullValue()));
  }

  @Test(expected = DataRetrievalException.class)
  public void shouldThrowExceptionIfThereIsOtherProblemWithFindingById() throws Exception {
    // given
    BaseCommunicationService<T> service = prepareService();
    UUID id = UUID.randomUUID();

    // when
    when(restTemplate.exchange(
        any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(service.getResultClass())
    )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

    service.findOne(id);
  }

  protected abstract T generateInstance();

  protected abstract BaseCommunicationService<T> getService();

  protected BaseCommunicationService prepareService() {
    BaseCommunicationService service = getService();
    service.setRestTemplate(restTemplate);
    service.setAuthorizationService(authService);

    return service;
  }

  protected void assertAuthHeader(HttpEntity entity) {
    assertThat(entity.getHeaders().get(HttpHeaders.AUTHORIZATION),
            is(singletonList("Bearer " + TOKEN)));
  }

  private void mockAuth() {
    when(authService.obtainAccessToken()).thenReturn(TOKEN);
  }

  private void checkAuth() {
    verify(authService, atLeastOnce()).obtainAccessToken();
  }

}

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

package org.openlmis.selv.report.dto.external.requisition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum RequisitionStatusDto {
  INITIATED(1),
  REJECTED(1),
  SUBMITTED(2),
  AUTHORIZED(3),
  IN_APPROVAL(3),
  APPROVED(4),
  RELEASED(5),
  RELEASED_WITHOUT_ORDER(6),
  SKIPPED(-1);

  private static final Map<RequisitionStatusDto, String> TRANSLATIONS =
      Collections.unmodifiableMap(new HashMap<RequisitionStatusDto, String>() {{
          put(INITIATED, "RASCUNHO");
          put(REJECTED, "REJEITADO");
          put(SUBMITTED, "SUBMETIDO");
          put(AUTHORIZED, "AUTORIZADO");
          put(IN_APPROVAL, "EM APROVAÇÃO");
          put(APPROVED, "APROVADO");
          put(RELEASED, "TERMINADO");
          put(RELEASED_WITHOUT_ORDER, "TERMINADO SEM ENCOMENDA");
          put(SKIPPED, "IGNORADO");
        }
      });

  private int value;

  RequisitionStatusDto(int value) {
    this.value = value;
  }

  @JsonIgnore
  public boolean isSubmittable() {
    return value == 1;
  }

  @JsonIgnore
  public boolean isUpdatable() {
    return value < 4 && value != -1;
  }

  @JsonIgnore
  public boolean isPreAuthorize() {
    return value == 1 || value == 2;
  }

  @JsonIgnore
  public boolean isPostSubmitted() {
    return value >= 2;
  }

  @JsonIgnore
  public boolean isApproved() {
    return value >= 4;
  }

  public boolean duringApproval() {
    return value == 3;
  }

  public boolean isAuthorized() {
    return value >= 3;
  }

  public String getTranslation() {
    return TRANSLATIONS.get(this);
  }
}

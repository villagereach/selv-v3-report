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

package org.openlmis.selv.report.databuilder.referencedata;

import java.util.UUID;
import org.openlmis.selv.report.dto.external.referencedata.FacilityTypeDto;

public class FacilityTypeDtoDataBuilder {

  private static int instanceNumber = 0;

  private UUID id;
  private String code;
  private String name;
  private String description;
  private Integer displayOrder;
  private Boolean active;

  /**
   * Constructor for setting default values to build {@link FacilityTypeDto}.
   */
  public FacilityTypeDtoDataBuilder() {
    instanceNumber++;

    id = UUID.randomUUID();
    code = "FT" + instanceNumber;
    name = "Facility Type " + instanceNumber;
    description = "facility type description " + instanceNumber;
    displayOrder = instanceNumber;
    active = true;
  }

  public FacilityTypeDto build() {
    return new FacilityTypeDto(id, code, name, description, displayOrder, active);
  }
}

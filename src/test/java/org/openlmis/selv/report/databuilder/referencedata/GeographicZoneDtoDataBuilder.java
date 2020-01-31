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
import org.openlmis.selv.report.dto.external.referencedata.GeographicLevelDto;
import org.openlmis.selv.report.dto.external.referencedata.GeographicZoneDto;

public class GeographicZoneDtoDataBuilder {

  private static int instanceNumber = 0;

  private UUID id;
  private String code;
  private String name;
  private GeographicLevelDto level;
  private GeographicZoneDto parent;

  /**
   * Constructor for setting default values to build {@link GeographicZoneDto}.
   */
  public GeographicZoneDtoDataBuilder() {
    instanceNumber++;

    id = UUID.randomUUID();
    code = "GZ" + instanceNumber;
    name = "geo zone " + instanceNumber;
    level = new GeographicLevelDtoDataBuilder().build();
    parent = null;
  }

  public GeographicZoneDto build() {
    return new GeographicZoneDto(id, code, name, level, parent);
  }
}

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

package org.openlmis.selv.report.dto.external.referencedata;

import static java.lang.Boolean.parseBoolean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderableDto {
  private UUID id;
  private static final String USE_VVM = "useVVM";

  private String productCode;
  private String fullProductName;
  private long netContent;
  private long packRoundingThreshold;
  private boolean roundToZero;
  private Set<ProgramOrderableDto> programs;
  private DispensableDto dispensable;
  private Map<String, String> extraData;

  public OrderableDto(String productCode, String fullProductName) {
    this(null, productCode, fullProductName, 0L, 0L, false, null, null, null);
  }

  @JsonIgnore
  public boolean useVvm() {
    return null != extraData && parseBoolean(extraData.get(USE_VVM));
  }

  /**
   * Get program orderable for given program id.
   * @return program orderable.
   */
  @JsonIgnore
  public ProgramOrderableDto findProgramOrderableDto(UUID programId) {
    return programs.stream().filter(po -> po.getProgramId().equals(programId))
        .findFirst().orElse(null);
  }
}

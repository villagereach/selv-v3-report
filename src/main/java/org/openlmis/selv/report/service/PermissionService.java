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

package org.openlmis.selv.report.service;

import static org.openlmis.selv.report.i18n.PermissionMessageKeys.ERROR_NO_PERMISSION;

import org.openlmis.selv.report.dto.external.ResultDto;
import org.openlmis.selv.report.dto.external.referencedata.RightDto;
import org.openlmis.selv.report.dto.external.referencedata.UserDto;
import org.openlmis.selv.report.exception.PermissionMessageException;
import org.openlmis.selv.report.service.referencedata.UserReferenceDataService;
import org.openlmis.selv.report.utils.AuthenticationHelper;
import org.openlmis.selv.report.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {
  static final String REPORT_TEMPLATES_EDIT = "REPORT_TEMPLATES_EDIT";
  public static final String REPORTS_VIEW = "REPORTS_VIEW";

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  public void canEditReportTemplates() {
    checkPermission(REPORT_TEMPLATES_EDIT);
  }

  /**
   * Check whether the user has REPORTS_VIEW permission.
   */
  public void canViewReports() {
    checkPermission(REPORTS_VIEW);
  }

  /**
   * Checks whether user has the given set of rights.
   * Throws {@link PermissionMessageException} if doesn't have all of these permissions.
   *
   * @param rights names of rights to check
   */
  public void validatePermissions(String... rights) {
    for (String right : rights) {
      if (!hasPermission(right)) {
        throw new PermissionMessageException(new Message(ERROR_NO_PERMISSION, right));
      }
    }
  }

  private void checkPermission(String rightName) {
    if (!hasPermission(rightName)) {
      throw new PermissionMessageException(new Message(ERROR_NO_PERMISSION, rightName));
    }
  }

  private Boolean hasPermission(String rightName) {
    UserDto user = authenticationHelper.getCurrentUser();
    RightDto right = authenticationHelper.getRight(rightName);
    ResultDto<Boolean> result = userReferenceDataService.hasRight(user.getId(), right.getId());
    return null != result && result.getResult();
  }
}
